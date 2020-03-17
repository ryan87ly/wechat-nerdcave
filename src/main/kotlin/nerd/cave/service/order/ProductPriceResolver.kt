package nerd.cave.service.order

import nerd.cave.model.api.product.Product
import nerd.cave.model.api.product.ProductDiscount
import nerd.cave.model.api.product.WeekDayDiscount
import nerd.cave.service.holiday.HolidayService
import java.time.Clock
import java.time.ZonedDateTime

class ProductPriceResolver(private val clock: Clock, private val holidayService: HolidayService) {

    suspend fun getPrice(product: Product): Int {
        val applicableDiscount = product.discounts.firstOrNull { it.isApplicable() }
        return applicableDiscount?.apply(product) ?: product.regularPrice
    }

    private suspend fun ProductDiscount.isApplicable(): Boolean {
        return when(detail) {
            is WeekDayDiscount -> detail.isApplicable()
            else -> false
        }
    }

    private fun ProductDiscount.apply(product: Product): Int {
        return when(detail) {
            is WeekDayDiscount -> detail.salePrice
            else -> product.regularPrice
        }
    }

    private suspend fun WeekDayDiscount.isApplicable(): Boolean {
        val date = ZonedDateTime.now(clock).toLocalDate()
        return !holidayService.isHoliday(date)
    }
}