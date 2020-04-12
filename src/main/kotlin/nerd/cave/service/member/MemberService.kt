package nerd.cave.service.member

import nerd.cave.model.api.member.Member
import nerd.cave.model.api.member.MemberContact
import nerd.cave.model.api.member.MemberDetail
import nerd.cave.model.api.product.Product

interface MemberService {
    suspend fun canPurchaseOnWechat(member: Member, products: List<Product>): Boolean
    suspend fun getEffectiveMemberDetail(member: Member): MemberDetail
    suspend fun getRawMembersInfo(start: Int, count: Int): List<Member>
    suspend fun getAllRawMembersInfo(): List<Member>
    suspend fun getRawMember(memberId: String): Member?
    suspend fun getRawMembers(memberIds: List<String>): List<Member>
    suspend fun updateMemberInfo(memberId:String, memberContact: MemberContact, memberDetail: MemberDetail): Boolean
    suspend fun spendEntry(member: Member): Boolean
}