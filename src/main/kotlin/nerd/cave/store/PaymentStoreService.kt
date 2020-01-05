package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.payment.Payment
import nerd.cave.web.wx.payment.WXPayResponse

interface PaymentStoreService: LifeCycle {
    suspend fun createPayment(payment: Payment)
    suspend fun updatePrepay(paymentId: String, prepayId: String, prepayInfo: WXPayResponse): Boolean
}