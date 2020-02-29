package nerd.cave.service.member

import nerd.cave.model.member.MemberDetail
import nerd.cave.model.product.Product

interface MemberService {
    suspend fun canPurchaseOnWechat(memberId: String, products: List<Product>): Boolean
    suspend fun getMemberDetail(memberId: String): MemberDetail
}