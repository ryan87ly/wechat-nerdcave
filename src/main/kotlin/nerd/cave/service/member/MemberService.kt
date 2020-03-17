package nerd.cave.service.member

import nerd.cave.model.api.member.Member
import nerd.cave.model.api.member.MemberDetail
import nerd.cave.model.api.product.Product

interface MemberService {
    suspend fun canPurchaseOnWechat(member: Member, products: List<Product>): Boolean
    suspend fun getEffectiveMemberDetail(member: Member): MemberDetail
    suspend fun spendEntry(member: Member): Boolean
}