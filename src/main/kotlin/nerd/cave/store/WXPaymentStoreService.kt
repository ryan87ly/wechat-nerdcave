package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.api.order.wechat.WXPayment
import nerd.cave.web.wx.payment.WXPayResponse
import java.time.ZonedDateTime

interface WXPaymentStoreService: LifeCycle {
    suspend fun createPayment(WXPayment: WXPayment)
    suspend fun updatePrepay(paymentId: String, prepayId: String, prepayInfo: WXPayResponse): Boolean
    suspend fun fetchPayment(paymentId: String): WXPayment?
    suspend fun updateRedeemedPayment(paymentId: String, transactionId: String, paymentCallback: WXPayResponse): Boolean
    suspend fun fetchPayments(startTimeInclusive: ZonedDateTime?, endTimeExclusive: ZonedDateTime?): List<WXPayment>
}