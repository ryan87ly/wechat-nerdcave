package nerd.cave.service.checkin

import nerd.cave.model.api.member.*
import nerd.cave.model.api.token.EnrichedToken
import nerd.cave.model.api.token.Token
import nerd.cave.service.branch.BranchService
import nerd.cave.service.member.MemberService
import nerd.cave.store.StoreService
import nerd.cave.util.MongoIdGenerator
import nerd.cave.util.toFormattedString
import nerd.cave.web.exceptions.BadRequestException
import java.time.Clock
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap

class CheckInServiceImpl(
    private val clock: Clock,
    storeService: StoreService,
    private val memberService: MemberService,
    private val branchService: BranchService,
    private val checkInNumberGenerator: CheckInNumberGenerator
): CheckInService {

    private val branchStoreService by lazy { storeService.branchStoreService }
    private val ticketStoreService by lazy { storeService.ticketStoreService }
    private val tokenStoreService by lazy { storeService.tokenStoreService }
    private val idGenerator = MongoIdGenerator()

    override suspend fun checkIn(member: Member, branchId: String): Token {
        val branch = branchStoreService.fetchById(branchId) ?: throw BadRequestException("Unable to find branch [$branchId]")
        val now = ZonedDateTime.now(clock).toLocalDateTime()
        if (!branchService.isBranchOpen(branch, now)) throw BadRequestException("Branch [$branchId] is closed")
        val checkInDate = now.toLocalDate()
        val token = tokenStoreService.fetchToken(member.id, checkInDate)
        if (token != null) throw BadRequestException("Member has already checked in on ${checkInDate.toFormattedString()}")
        return when(val effectiveMemberDetail = memberService.getEffectiveMemberDetail(member)) {
                is UnlimitedEntriesMemberDetail -> createTokenForUnlimitedMember(member.id, effectiveMemberDetail.memberType, branch.id, checkInDate)
                is MultiEntriesMember -> createTokenForMultiEntriesMember(member, branch.id, checkInDate)
                is NormalMember -> if (effectiveMemberDetail.remainingEntries > 0) createTokenFromTicket(member.id, branch.id, checkInDate) else throw BadRequestException("No available ticket found!")
                else -> throw BadRequestException("Unsupported member")
            }
    }

    override suspend fun fetchToken(memberId: String, checkInDate: LocalDate): Token? {
        return tokenStoreService.fetchToken(memberId, checkInDate)
    }

    private suspend fun createTokenForMultiEntriesMember(member: Member, branchId: String, checkInDate: LocalDate): Token {
        return if (memberService.spendEntry(member)) {
            val token = generateToken(member.id, MemberType.MULTI_ENTRIES, branchId, false, checkInDate)
            tokenStoreService.insertToken(token)
            token
        } else throw BadRequestException("Insufficient entry!")
    }

    private suspend fun createTokenFromTicket(memberId: String, branchId: String, checkInDate: LocalDate): Token {
        return ticketStoreService.latestNotUsedTicket(memberId)
            ?.let {
                val token = generateToken(memberId, MemberType.NORMAL, branchId, it.hasEquipment, checkInDate)
                if (ticketStoreService.markUsed(it.id, token.id)) {
                    tokenStoreService.insertToken(token)
                    token
                } else {
                    throw BadRequestException("No available ticket found!")
                }
            } ?: throw BadRequestException("No available ticket found!")
    }

    private suspend fun createTokenForUnlimitedMember(memberId: String, memberType: MemberType, branchId: String, checkInDate: LocalDate): Token {
        val token = generateToken(memberId, memberType, branchId, false, checkInDate)
        tokenStoreService.insertToken(token)
        return token
    }

    private suspend fun generateToken(memberId: String, memberType: MemberType, branchId: String, hasEquipment: Boolean, checkInDate: LocalDate): Token {
        val now = ZonedDateTime.now(clock)
        val checkInNumber = checkInNumberGenerator.nextNumber(checkInDate, branchId)
        return Token(
            idGenerator.nextId(),
            branchId,
            memberId,
            memberType,
            hasEquipment,
            checkInNumber,
            now,
            checkInDate
        )
    }

    override suspend fun checkInHistory(memberId: String, startDateInclusive: LocalDate, endDateExclusive: LocalDate?): List<String> {
        return tokenStoreService.history(memberId, startDateInclusive, endDateExclusive)
    }

    override suspend fun allCheckInHistory(): List<EnrichedToken> {
        val tokens = tokenStoreService.allHistories()
            .sortedBy { it.checkInTime }
        return toEnrichedTokens(tokens)
    }

    override suspend fun membersCheckInHistory(startDateInclusive: LocalDate, endDateExclusive: LocalDate?): List<EnrichedToken> {
        val tokens = tokenStoreService.histories(startDateInclusive, endDateExclusive)
        return toEnrichedTokens(tokens)
    }

    private suspend fun toEnrichedTokens(tokens: List<Token>): List<EnrichedToken> {
        val memberIds = tokens.map { it.memberId }
            .distinct()
        val members = memberService.getRawMembers(memberIds)
            .associateBy { it.id }
        val branchNames = ConcurrentHashMap<String, String>()
        return tokens.map { token ->
            EnrichedToken(
                token.branchId,
                branchNames.getOrPut(token.branchId) { branchStoreService.fetchById(token.branchId)?.name ?: "<Unknown branch>" },
                token.memberId,
                members[token.memberId]?.memberContact?.legalName ?: "<Unknown member>",
                members[token.memberId]?.memberContact?.contactNumber ?: "<Unknown contact>",
                token.memberType,
                token.checkInTime,
                token.checkInDate
            )
        }
    }

    override suspend fun countByMemberId(startDateInclusive: LocalDate, endDateExclusive: LocalDate?): List<Pair<String, Long>> {
        return tokenStoreService.countByMemberId(startDateInclusive, endDateExclusive)
    }

    override suspend fun fetchRecentCheckIns(branchId: String): Long {
        val startTimeInclusive = ZonedDateTime.now(clock).plusHours(-2)
        return tokenStoreService.countByBranch(branchId, startTimeInclusive)
    }


}