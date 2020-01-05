package nerd.cave.service.member

import nerd.cave.model.member.Member
import nerd.cave.model.product.Product

interface MemberService {
    fun canPurchaseOnWechat(member: Member, products: List<Product>): Boolean
}