package nerd.cave.model.api.order.wechat

import java.time.ZonedDateTime

data class WXPayment (
    val id: String,
    val memberId: String,
    val openid: String,
    val time: ZonedDateTime,
    val items: List<PaymentItem>,
    val totalFee: Int,
    val branchId: String,
    val status: PaymentStatus,
    val wxPrepayId: String? = null,
    val wxPrepayInfo: Map<String, String>? = null,
    val wxTransactionId: String? = null,
    val wxPaymentCallback: Map<String, String>? = null,
    val wxPaymentEndTime: String? = null
)

enum class PaymentStatus {
    INITIAL,
    PREPAY,
    COMPLETED
}