package nerd.cave.service.member

import nerd.cave.model.member.Member
import nerd.cave.model.member.isNormalMember
import nerd.cave.model.product.Product
import java.time.Clock
import java.time.LocalDate

class MemberServiceImpl(private val clock: Clock): MemberService {

    override fun canPurchaseOnWechat(member: Member, products: List<Product>): Boolean {
        return member.isNormalMember(LocalDate.now(clock)) &&
            products.all { it.enabled } &&
            products.all { it.payViaWechat }
    }

}