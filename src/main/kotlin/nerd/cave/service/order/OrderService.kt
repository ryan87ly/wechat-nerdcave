package nerd.cave.service.order

import nerd.cave.model.admin.Account
import nerd.cave.model.api.branch.Branch
import nerd.cave.model.api.order.EnrichedOrder
import nerd.cave.model.api.order.offline.OfflineOrder
import nerd.cave.model.api.order.wechat.WXPayment
import nerd.cave.model.api.product.Product
import nerd.cave.web.wx.payment.WXPayResponse
import java.time.LocalDateTime

interface OrderService {
    suspend fun newOfflineOrder(memberId: String, product: Product, branch: Branch): OfflineOrder
    suspend fun approveOfflineOrder(orderId: String, approver: Account)
    suspend fun orders(startLocalTimeInclusive: LocalDateTime?, endLocalTimeExclusive: LocalDateTime?): List<EnrichedOrder>
    suspend fun newPayment(memberId: String, openid: String, branchId:String, products: List<Product>): WXPayment
    suspend fun updatePrepay(paymentId: String, prepayId: String, prepayInfo: WXPayResponse): Boolean
    suspend fun fetchPayment(paymentId: String): WXPayment?
    suspend fun redeemPayment(WXPayment: WXPayment, transactionId: String, paymentCallback: WXPayResponse)
}