package nerd.cave.model.api.order

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import nerd.cave.model.api.order.offline.ApprovalInfo
import nerd.cave.model.api.order.offline.OfflineOrderStatus
import nerd.cave.model.api.order.wechat.PaymentStatus
import nerd.cave.model.api.product.ProductType
import nerd.cave.util.LocalDateTimeSerializer
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class EnrichedOrder (
    val id: String,
    val memberName: String,
    val contactNumber: String,
    @get:JsonSerialize(using = LocalDateTimeSerializer::class)
    val time: LocalDateTime,
    val productType: ProductType,
    val description: String,
    val branchName: String,
    val orderType: OrderType,
    val status: OrderStatus,
    val approvalInfo: EnrichedApprovalInfo? = null
)

data class EnrichedApprovalInfo(
    val approverId: String,
    val approverName: String,
    @get:JsonSerialize(using = LocalDateTimeSerializer::class)
    val time: LocalDateTime
)

enum class OrderStatus {
    PENDING_WX_PAYMENT,
    WX_PAYMENT_COMPLETED,
    UNAPPROVED,
    APPROVED;

    companion object {
        fun fromOfflineOrderStatus(offlineOrderStatus: OfflineOrderStatus): OrderStatus {
            return when(offlineOrderStatus) {
                OfflineOrderStatus.UNAPPROVED -> UNAPPROVED
                OfflineOrderStatus.APPROVED -> APPROVED
            }
        }

        fun fromWXPaymentOrderStatus(paymentStatus: PaymentStatus): OrderStatus {
            return when(paymentStatus) {
                PaymentStatus.INITIAL -> PENDING_WX_PAYMENT
                PaymentStatus.PREPAY -> PENDING_WX_PAYMENT
                PaymentStatus.COMPLETED -> WX_PAYMENT_COMPLETED
            }
        }
    }
}

enum class OrderType {
    OFFLINE,
    WECHAT
}
