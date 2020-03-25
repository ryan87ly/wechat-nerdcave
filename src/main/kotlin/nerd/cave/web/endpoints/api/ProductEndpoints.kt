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
import nerd.cave.model.api.product.Product
import nerd.cave.service.holiday.HolidayService
import nerd.cave.service.order.ProductPriceResolver
import nerd.cave.store.ProductStoreService
import nerd.cave.store.StoreService
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.exceptions.ResourceNotFoundException
import nerd.cave.web.extentions.coroutine
import nerd.cave.web.extentions.endIfOpen
import nerd.cave.web.extentions.ok
import nerd.cave.web.session.NerdCaveSessionHandler
import org.slf4j.LoggerFactory
import java.time.Clock

class ProductEndpoints(
    vertx: Vertx,
    sessionHandler: NerdCaveSessionHandler,
    clock: Clock,
    storeService: StoreService,
    private val holidayService: HolidayService
): HttpEndpoint {
    companion object {
        private val logger = LoggerFactory.getLogger(ProductEndpoints::class.java)
    }

    private val productStoreService: ProductStoreService by lazy { storeService.productStoreService }
    private val productPriceResolver = ProductPriceResolver(clock, holidayService)

    override val router: Router = Router.router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        route(sessionHandler.handler)
        get("/all") { allProducts(it) }
        get("/:id") { productById(it)}
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