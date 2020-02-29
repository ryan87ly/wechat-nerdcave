package nerd.cave.service.checkin

import nerd.cave.model.member.ContractMemberDetail
import nerd.cave.model.member.NormalMember
import nerd.cave.model.token.Token
import nerd.cave.model.token.toTokenDateFormat
import nerd.cave.service.member.MemberService
import nerd.cave.store.StoreService
import nerd.cave.util.MongoIdGenerator
import nerd.cave.web.exceptions.BadRequestException
import java.time.Clock
import java.time.LocalDate
import java.time.ZonedDateTime

class CheckInServiceImpl(
    private val clock: Clock,
    storeService: StoreService,
    private val memberService: MemberService,
    private val checkInNumberGenerator: CheckInNumberGenerator
): CheckInService {

    private val branchStoreService by lazy { storeService.branchStoreService }
    private val ticketStoreService by lazy { storeService.ticketStoreService }
    private val tokenStoreService by lazy { storeService.tokenStoreService }
    private val idGenerator = MongoIdGenerator()

    override suspend fun checkIn(memberId: String, branchId: String): Token? {
        val branch = branchStoreService.fetchById(branchId) ?: throw BadRequestException("Unable to find branch [$branchId]")
        val checkInDate = ZonedDateTime.now(clock).toLocalDate()
        val token = tokenStoreService.fetchToken(memberId, checkInDate)
        if (token != null) throw BadRequestException("Member has already checked in on ${checkInDate.toTokenDateFormat()}")
        return when(val memberDetail = memberService.getMemberDetail(memberId)) {
                is ContractMemberDetail -> createTokenForContractMember(memberId, branch.id, checkInDate)
                is NormalMember -> if (memberDetail.remainingEntries > 0) createTokenFromTicket(memberId, branch.id, checkInDate) else null
                else -> null
            }
    }

    override suspend fun fetchToken(memberId: String, checkInDate: LocalDate): Token? {
        return tokenStoreService.fetchToken(memberId, checkInDate)
    }

    private suspend fun createTokenFromTicket(memberId: String, branchId: String, checkInDate: LocalDate): Token? {
        return ticketStoreService.latestNotUsedTicket(memberId)
            ?.let {
                val token = generateToken(memberId, branchId, it.hasEquipment, checkInDate)
                if (ticketStoreService.markUsed(it.id, token.id)) {
                    tokenStoreService.insertToken(token)
                    token
                } else {
                    null
                }
            }
    }

    private suspend fun createTokenForContractMember(memberId: String, branchId: String, checkInDate: LocalDate): Token {
        val token = generateToken(memberId, branchId, false, checkInDate)
        tokenStoreService.insertToken(token)
        return token
    }

    private suspend fun generateToken(memberId: String, branchId: String, hasEquipment: Boolean, checkInDate: LocalDate): Token {
        val now = ZonedDateTime.now(clock)
        val checkInNumber = checkInNumberGenerator.nextNumber(checkInDate, branchId)
        return Token(
            idGenerator.nextId(),
            branchId,
            memberId,
            hasEquipment,
            checkInNumber,
            now,
            checkInDate.toTokenDateFormat()
        )
    }

    override suspend fun checkInHistory(memberId: String, startDateInclusive: LocalDate, endDateExclusive: LocalDate?): List<String> {
        return tokenStoreService.history(memberId, startDateInclusive, endDateExclusive)
    }




}