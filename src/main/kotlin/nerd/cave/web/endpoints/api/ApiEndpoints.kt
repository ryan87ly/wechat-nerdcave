package nerd.cave.web.endpoints.api

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import nerd.cave.service.member.MemberService
import nerd.cave.service.payment.PaymentService
import nerd.cave.store.StoreService
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.endpoints.api.action.ActionEndpoints
import nerd.cave.web.endpoints.api.login.LoginEndpoints
import nerd.cave.web.endpoints.api.payment.PaymentEndpoints
import nerd.cave.web.endpoints.api.product.ProductEndpoints
import nerd.cave.web.endpoints.notification.NotificationEndpoints
import nerd.cave.web.session.NerdCaveSessionHandler
import nerd.cave.web.wx.WXWebClient
import nerd.cave.web.wx.payment.WXPayClient
import java.time.Clock

class ApiEndpoints (
    vertx: Vertx,
    clock: Clock,
    wxWebClient: WXWebClient,
    wxPayClient: WXPayClient,
    storeService: StoreService,
    sessionHandler: NerdCaveSessionHandler,
    memberService: MemberService,
    paymentService: PaymentService
): HttpEndpoint {

    override val router = Router.router(vertx).apply {
        mountSubRouter("/login", LoginEndpoints(vertx, wxWebClient, storeService.sessionStoreService, storeService.memberStoreService).router)
        mountSubRouter("/notification", NotificationEndpoints(vertx).router)
        mountSubRouter("/action", ActionEndpoints(vertx, sessionHandler).router)
        mountSubRouter("/product", ProductEndpoints(vertx, storeService.productStoreService, sessionHandler).router)
        mountSubRouter("/payment", PaymentEndpoints(vertx, clock, wxPayClient, sessionHandler, memberService, paymentService, storeService.productStoreService).router)
        options().handler { ctx ->
            ctx.response().apply {
                headers().apply {
                    set("Access-Control-Allow-Origin", "*")
                    set("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
                }
                statusCode = HttpResponseStatus.NO_CONTENT.code()
                end()
            }
        }
    }
}