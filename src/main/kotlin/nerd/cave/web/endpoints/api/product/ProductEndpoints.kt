package nerd.cave.web.endpoints.api.product

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.json.JsonObject.mapFrom
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.enum.fromString
import nerd.cave.model.product.*
import nerd.cave.store.ProductStoreService
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.exceptions.BadRequestException
import nerd.cave.web.exceptions.ResourceNotFoundException
import nerd.cave.web.extentions.coroutine
import nerd.cave.web.extentions.endIfOpen
import nerd.cave.web.session.NerdCaveSessionHandler
import org.slf4j.LoggerFactory
import java.util.*

class ProductEndpoints(vertx: Vertx, private val productStoreService: ProductStoreService, sessionHandler: NerdCaveSessionHandler): HttpEndpoint {
    companion object {
        private val logger = LoggerFactory.getLogger(ProductEndpoints::class.java)
    }

    override val router: Router = Router.router(vertx).coroutine(vertx.dispatcher(), ProductEndpoints.logger).apply {
        route(sessionHandler.handler)
        post("/") { newProduct(it) }
        get("/products") { allProducts(it) }
        get("/:id") { productById(it)}
        delete("/:id") { deleteProductById(it) }
    }

    private suspend fun allProducts(ctx: RoutingContext) {
        val products = productStoreService.fetchAll()
        ctx.response().endIfOpen(jsonArrayOf(*products.toTypedArray()))
    }

    private suspend fun productById(ctx: RoutingContext) {
        val productId = ctx.request().params().get("id")
        val product = productStoreService.fetchById(productId) ?: throw ResourceNotFoundException("Product id: [$productId] not found")
        ctx.response().endIfOpen(mapFrom(product))
    }

    private suspend fun deleteProductById(ctx: RoutingContext) {
        val productId = ctx.request().params().get("id")
        val deleteResult = productStoreService.deleteById(productId)
        if(deleteResult) ctx.response().endIfOpen(HttpResponseStatus.NO_CONTENT) else throw ResourceNotFoundException("Product id: [$productId] not found")
    }

    private suspend fun newProduct(ctx: RoutingContext) {
        val productTypeStr = ctx.bodyAsJson.getString("productType")
        val productType = fromString<ProductType>(productTypeStr) ?: throw BadRequestException("Unable to find product type [$productTypeStr]")
        val id = UUID.randomUUID().toString()
        val detailJson = ctx.bodyAsJson.getJsonObject("detail")
        val detail = toProductDetail(productType, detailJson)
        val description = ctx.bodyAsJson.getString("description")
        val fee = ctx.bodyAsJson.getInteger("fee")
        val payViaWechat = ctx.bodyAsJson.getBoolean("payViaWechat")
        val enabled = ctx.bodyAsJson.getBoolean("enabled")
        val product = Product(id, productType, detail, description, fee, payViaWechat, enabled)
        productStoreService.createProduct(product)
        ctx.response()
            .endIfOpen(
                HttpResponseStatus.CREATED,
                jsonObjectOf(
                    "id" to id
                ).encodePrettily()
            )
    }

    private fun toProductDetail(productType: ProductType, detail: JsonObject): ProductDetail {
        return when(productType) {
            ProductType.LIMITED_ENTRIES -> LimitedEntriesProduct(detail.getInteger("entries"))
            ProductType.MONTHLY_MEMBER_FEE -> MonthlyMemberProduct(detail.getInteger("months"))
            ProductType.ANNUAL_MEMBER_FEE -> AnnualMemberProduct(detail.getInteger("years"))
            ProductType.EQUIPMENT_RENTAL_FEE -> EquipmentRentalProduct()
            ProductType.SINGLE_ENTRY_FEE -> SingleEntryProduct()
        }
    }

}