package nerd.cave.web.endpoints.api

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.service.order.OrderService
import nerd.cave.store.StoreService
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.exceptions.BadRequestException
import nerd.cave.web.extentions.coroutine
import nerd.cave.web.extentions.getMandatoryString
import nerd.cave.web.extentions.ok
import nerd.cave.web.session.NerdCaveSessionHandler
import nerd.cave.web.session.nerdCaveMember
import org.slf4j.LoggerFactory

class OfflineOrderEndpoints(
    vertx: Vertx,
    sessionHandler: NerdCaveSessionHandler,
    storeService: StoreService,
    private val offlineOrderService: OrderService
): HttpEndpoint {
    companion object {
        val logger = LoggerFactory.getLogger(OfflineOrderEndpoints::class.java)
    }

    private val productStoreService by lazy { storeService.productStoreService }
    private val branchStoreService by lazy { storeService.branchStoreService }

    override val router: Router = Router.router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        route(sessionHandler.handler)
        post("/placeOrder") { placeOrder(it) }
    }

    private suspend fun placeOrder(ctx: RoutingContext) {
        val member = ctx.nerdCaveMember()
        val branchId = ctx.bodyAsJson.getMandatoryString("branchId")
        val productId = ctx.bodyAsJson.getMandatoryString("productId")
        val branch = branchStoreService.fetchById(branchId) ?: throw BadRequestException("Branch not found [$branchId]")
        val product = productStoreService.fetchById(productId) ?: throw BadRequestException("Product not found [$productId]")
        if (!product.enabled) throw BadRequestException("Product [$productId] has already been disabled")
        if (product.payViaWechat) throw BadRequestException("Product [$productId] can only place via wechat")
        val order = offlineOrderService.newOfflineOrder(member.id, product, branch)
        ctx.response().ok(
            jsonObjectOf(
                "id" to order.id
            )
        )
    }

}