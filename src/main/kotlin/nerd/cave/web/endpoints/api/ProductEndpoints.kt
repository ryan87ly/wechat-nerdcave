package nerd.cave.web.endpoints.api

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.json.JsonObject.mapFrom
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.model.api.product.*
import nerd.cave.service.holiday.HolidayService
import nerd.cave.service.order.ProductPriceResolver
import nerd.cave.store.ProductStoreService
import nerd.cave.util.MongoIdGenerator
import nerd.cave.util.fromString
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.exceptions.BadRequestException
import nerd.cave.web.exceptions.ResourceNotFoundException
import nerd.cave.web.extentions.*
import nerd.cave.web.session.NerdCaveSessionHandler
import org.slf4j.LoggerFactory
import java.time.Clock

class ProductEndpoints(
    vertx: Vertx,
    sessionHandler: NerdCaveSessionHandler,
    clock: Clock,
    private val productStoreService: ProductStoreService,
    private val holidayService: HolidayService
): HttpEndpoint {
    private val idGenerator = MongoIdGenerator()
    private val productPriceResolver = ProductPriceResolver(clock, holidayService)

    companion object {
        private val logger = LoggerFactory.getLogger(ProductEndpoints::class.java)
    }

    override val router: Router = Router.router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        route(sessionHandler.handler)
        post("/") { newProduct(it) }
        get("/all") { allProducts(it) }
        get("/:id") { productById(it)}
        delete("/:id") { deleteProductById(it) }
    }

    private suspend fun allProducts(ctx: RoutingContext) {
        val products = productStoreService.fetchAll()
        ctx.response().ok(
            jsonArrayOf(
                *products.map { it.toJson() }.toTypedArray()
            )
        )
    }

    private suspend fun productById(ctx: RoutingContext) {
        val productId = ctx.request().params().get("id")
        val product = productStoreService.fetchById(productId) ?: throw ResourceNotFoundException("Product id: [$productId] not found")
        ctx.response().ok(product.toJson())
    }

    private suspend fun deleteProductById(ctx: RoutingContext) {
        val productId = ctx.request().params().get("id")
        val deleteResult = productStoreService.deleteById(productId)
        if(deleteResult) ctx.response().endIfOpen(HttpResponseStatus.NO_CONTENT) else throw ResourceNotFoundException("Product id: [$productId] not found")
    }

    private suspend fun newProduct(ctx: RoutingContext) {
        val product = ctx.bodyAsJson.toProduct()
        productStoreService.createProduct(product)
        ctx.response()
            .endIfOpen(
                HttpResponseStatus.CREATED,
                jsonObjectOf(
                    "id" to product.id
                ).encodePrettily()
            )
    }

    private fun JsonObject.toProduct(): Product {
        val productTypeStr = getMandatoryString("productType")
        val productType = fromString<ProductType>(productTypeStr) ?: throw BadRequestException("Unsupported product type [$productTypeStr]")
        val id = idGenerator.nextId()
        val detailJson = getJsonObject("detail")
        val detail = detailJson.toProductDetail(productType)
        val shortDescription = getMandatoryString("shortDescription")
        val description = getMandatoryString("description")
        val regularPrice = getMandatoryInt("regularPrice")
        val discounts = getJsonArray("discounts").map { (it as? JsonObject)?.toProductDiscount() ?: throw BadRequestException("Discount should be Json object") }
        val payViaWechat = getBoolean("payViaWechat")
        val enabled = getBoolean("enabled")
        return Product(id, productType, detail, shortDescription, description, discounts, regularPrice, payViaWechat, enabled)
    }

    private fun JsonObject.toProductDetail(productType: ProductType): ProductDetail {
        return when(productType) {
            ProductType.MULTI_ENTRIES_FEE -> MultiEntriesProduct(getMandatoryInt("entries"), getMandatoryInt("validMonths"))
            ProductType.MONTHLY_MEMBER_FEE -> MonthlyMemberProduct(getMandatoryInt("months"))
            ProductType.YEARLY_MEMBER_FEE -> YearlyMemberProduct(getMandatoryInt("years"))
            ProductType.EQUIPMENT_RENTAL_FEE -> EquipmentRentalProduct()
            ProductType.SINGLE_ENTRY_FEE -> SingleEntryProduct()
        }
    }

    private fun JsonObject.toProductDiscount(): ProductDiscount {
        val typeStr = getMandatoryString("type")
        val type = fromString<DiscountType>(typeStr) ?: throw BadRequestException("Unsupported discount type [$typeStr]")
        val description = getMandatoryString("description")
        val detailObj = getJsonObject("detail")
        val detail = detailObj.toDiscountDetail(type)
        return ProductDiscount(type, description, detail)
    }

    private fun JsonObject.toDiscountDetail(type: DiscountType): DiscountDetail {
        return when(type) {
            DiscountType.WEEK_DAY -> WeekDayDiscount(getMandatoryInt("salePrice"))
        }
    }

    private suspend fun Product.toJson(): JsonObject {
        return jsonObjectOf(
            "id" to id,
            "productType" to productType,
            "detail" to mapFrom(detail),
            "description" to description,
            "regularPrice" to regularPrice,
            "fee" to productPriceResolver.getPrice(this),
            "discounts" to jsonArrayOf(*discounts.toTypedArray()),
            "payViaWechat" to payViaWechat,
            "enabled" to enabled
        )
    }

}