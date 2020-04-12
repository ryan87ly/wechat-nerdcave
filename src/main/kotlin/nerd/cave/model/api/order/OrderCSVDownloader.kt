package nerd.cave.model.api.order

import nerd.cave.model.api.product.ProductType
import nerd.cave.util.CSVWriter
import nerd.cave.util.toFormattedString
import nerd.cave.util.toSpreadSheetString

class OrderCSVDownloader(private val orders: List<EnrichedOrder>) {
    private val headers = linkedMapOf(
        "date" to "日期",
        "time" to "时间",
        "legalName" to "用户",
        "contactNumber" to "电话",
        "cardType" to "卡类",
        "branchName" to "下单地点",
        "orderStatus" to "订单状态"
    )

    fun toCSVString(): String {
        return CSVWriter(headers)
            .addRows( *orders.map { it.toMap() }.toTypedArray() )
            .toCSVString()
    }

    private fun EnrichedOrder.toMap(): Map<String, String?> {
        return mapOf(
            "date" to this.time.toLocalDate().toFormattedString(),
            "time" to this.time.toLocalTime().toSpreadSheetString(),
            "legalName" to this.memberName,
            "contactNumber" to this.contactNumber,
            "cardType" to this.productType.toDisplayName(),
            "branchName" to this.branchName,
            "orderStatus" to this.status.toDisplayName()
        )
    }

    private fun ProductType.toDisplayName(): String {
        return when(this) {
            ProductType.MULTI_ENTRIES_FEE -> "次卡"
            ProductType.MONTHLY_MEMBER_FEE -> "月卡"
            ProductType.YEARLY_MEMBER_FEE -> "年卡"
            ProductType.SINGLE_ENTRY_FEE -> "普通"
            ProductType.EQUIPMENT_RENTAL_FEE -> "装备"
        }
    }

    private fun OrderStatus.toDisplayName(): String {
        return when(this) {
            OrderStatus.PENDING_WX_PAYMENT -> "待支付"
            OrderStatus.WX_PAYMENT_COMPLETED -> "自动通过"
            OrderStatus.UNAPPROVED -> "未通过"
            OrderStatus.APPROVED -> "已通过"
        }
    }

}