package nerd.cave.model.api.order.wechat

import nerd.cave.model.api.product.ProductType

data class PaymentItem (
    val productId: String,
    val productType: ProductType,
    val description: String,
    val itemFee: Int
)

