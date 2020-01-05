package nerd.cave.model.product

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class Product(
    val id: String,
    val productType: ProductType,
    val detail: ProductDetail,
    val description: String,
    val fee: Int, // in 0.01 RMB
    val payViaWechat: Boolean,
    val enabled: Boolean
)

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="productType")
@JsonSubTypes(
    JsonSubTypes.Type(value = LimitedEntriesProduct::class, name = "LIMITED_ENTRIES"),
    JsonSubTypes.Type(value = MonthlyMemberProduct::class, name = "MONTHLY_MEMBER_FEE"),
    JsonSubTypes.Type(value = AnnualMemberProduct::class, name = "ANNUAL_MEMBER_FEE"),
    JsonSubTypes.Type(value = EquipmentRentalProduct::class, name = "EQUIPMENT_RENTAL_FEE"),
    JsonSubTypes.Type(value = SingleEntryProduct::class, name = "SINGLE_ENTRY_FEE")
)
interface ProductDetail

data class LimitedEntriesProduct(
    val entries: Int
): ProductDetail

data class MonthlyMemberProduct(
    val months: Int
): ProductDetail

data class AnnualMemberProduct(
    val years: Int
): ProductDetail

class SingleEntryProduct: ProductDetail
class EquipmentRentalProduct: ProductDetail

enum class ProductType {
    LIMITED_ENTRIES,
    MONTHLY_MEMBER_FEE,
    ANNUAL_MEMBER_FEE,
    SINGLE_ENTRY_FEE,
    EQUIPMENT_RENTAL_FEE,
}
