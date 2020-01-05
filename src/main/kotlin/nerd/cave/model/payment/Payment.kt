package nerd.cave.model.payment

data class Payment (
    val id: String,
    val memberId: String,
    val openid: String,
    val items: List<PaymentItem>,
    val totalFee: Int,
    val status: PaymentStatus,
    val wxPrepayId: String? = null,
    val wxPrepayInfo: Map<String, String>? = null,
    val wxTransactionId: String? = null
)

enum class PaymentStatus {
    Initial,
    Prepay,
    Complete,
    Aborted
}