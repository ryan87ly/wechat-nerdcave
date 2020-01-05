package nerd.cave.service.payment

import nerd.cave.model.payment.Payment
import nerd.cave.model.product.Product
import nerd.cave.web.wx.payment.WXPayResponse

interface PaymentService {
    suspend fun newPayment(memberId: String, openid: String, products: List<Product>): Payment
    suspend fun updatePrepay(paymentId: String, prepayId: String, prepayInfo: WXPayResponse): Boolean
}