package nerd.cave.model.api.order.offline

import nerd.cave.model.api.product.ProductDetail
import nerd.cave.model.api.product.ProductType
import java.time.ZonedDateTime

data class OfflineOrder(
    val id: String,
    val memberId: String,
    val time: ZonedDateTime,
    val item: OfflineOrderItem,
    val branchId: String,
    val status: OfflineOrderStatus,
    val approvalInfo: ApprovalInfo? = null
)

enum class OfflineOrderStatus {
    UNAPPROVED,
    APPROVED
}

data class OfflineOrderItem(
    val productId: String,
    val productType: ProductType,
    val detail: ProductDetail,
    val description: String
)

data class ApprovalInfo(
    val approverId: String,
    val time: ZonedDateTime
)
