package nerd.cave.service.member

import nerd.cave.model.api.member.*
import nerd.cave.model.api.product.Product
import nerd.cave.store.StoreService
import nerd.cave.store.mongo.MongoStoreService
import nerd.cave.store.mongo.`in`
import nerd.cave.store.mongo.eq
import nerd.cave.web.exceptions.BadRequestException
import java.time.Clock
import java.time.ZonedDateTime

class MemberServiceImpl(private val clock: Clock, storeService: StoreService): MemberService {
    private val ticketStoreService by lazy { storeService.ticketStoreService }
    private val memberStoreService by lazy { storeService.memberStoreService }

    override suspend fun canPurchaseOnWechat(member: Member, products: List<Product>): Boolean {
        val memberDetail = getEffectiveMemberDetail(member)
        return memberDetail.memberType == MemberType.NORMAL &&
            products.all { it.enabled } &&
            products.all { it.payViaWechat }
    }

    override suspend fun getEffectiveMemberDetail(member: Member): MemberDetail {
        val now = ZonedDateTime.now(clock).toLocalDate()
        val memberId = member.id
        return when(val memberDetail = member.memberDetail) {
            is NormalMember -> NormalMember(ticketStoreService.countNotUsedTickets(memberId))
            is MultiEntriesMember -> if(memberDetail.isExpired(now) || memberDetail.totalEntries <= memberDetail.usedEntries) NormalMember(ticketStoreService.countNotUsedTickets(memberId)) else memberDetail
            is SpecialMemberDetail -> if(memberDetail.isExpired(now)) NormalMember(ticketStoreService.countNotUsedTickets(memberId)) else memberDetail
            else -> memberDetail
        }
    }

    override suspend fun getRawMembersInfo(start: Int, count: Int): List<Member> {
        return memberStoreService.fetchMembers(start, count)
    }

    override suspend fun getAllRawMembersInfo(): List<Member> {
        return memberStoreService.fecthAllMembers()
    }

    override suspend fun getRawMember(memberId: String): Member? {
        return memberStoreService.fetchById(memberId)
    }

    override suspend fun getRawMembers(memberIds: List<String>): List<Member> {
        return memberStoreService.fetchByIds(memberIds)
    }

    override suspend fun updateMemberInfo(memberId:String, memberContact: MemberContact, memberDetail: MemberDetail): Boolean {
        return memberStoreService.updateMemberInfo(memberId, memberContact, memberDetail)
    }

    override suspend fun spendEntry(member: Member): Boolean {
        if (member.memberDetail !is MultiEntriesMember) throw BadRequestException("Only MultiEntries member is allowed to call useEntry()")
        return memberStoreService.spendMemberEntry(member.id)
    }
}