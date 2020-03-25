package nerd.cave.model.api.product

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val productType: ProductType,
    val detail: ProductDetail,
    val discounts: List<ProductDiscount>,
    val regularPrice: Int, // in 0.01 RMB
    val payViaWechat: Boolean,
    val enabled: Boolean
)

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="productType")
@JsonSubTypes(
    JsonSubTypes.Type(value = MultiEntriesProduct::class, name = "MULTI_ENTRIES_FEE"),
    JsonSubTypes.Type(value = MonthlyMemberProduct::class, name = "MONTHLY_MEMBER_FEE"),
    JsonSubTypes.Type(value = YearlyMemberProduct::class, name = "YEARLY_MEMBER_FEE"),
    JsonSubTypes.Type(value = EquipmentRentalProduct::class, name = "EQUIPMENT_RENTAL_FEE"),
    JsonSubTypes.Type(value = SingleEntryProduct::class, name = "SINGLE_ENTRY_FEE")
)
interface ProductDetail

data class MultiEntriesProduct(
    val entries: Int,
    val validMonths: Int
): ProductDetail

data class MonthlyMemberProduct(
    val months: Int
): ProductDetail

data class YearlyMemberProduct(
    val years: Int
): ProductDetail

class SingleEntryProduct: ProductDetail
class EquipmentRentalProduct: ProductDetail

enum class ProductType {
    MULTI_ENTRIES_FEE,
    MONTHLY_MEMBER_FEE,
    YEARLY_MEMBER_FEE,
    SINGLE_ENTRY_FEE,
    EQUIPMENT_RENTAL_FEE,
}

data class ProductDiscount (
    val discountType: DiscountType,
    val description: String,
    val detail: DiscountDetail
)

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="discountType")
@JsonSubTypes(
    JsonSubTypes.Type(value = WeekDayDiscount::class, name = "WEEK_DAY")
)
interface DiscountDetail

data class WeekDayDiscount(
    val salePrice: Int // in 0.01 RMB
): DiscountDetail

enum class DiscountType {
    WEEK_DAY
}
