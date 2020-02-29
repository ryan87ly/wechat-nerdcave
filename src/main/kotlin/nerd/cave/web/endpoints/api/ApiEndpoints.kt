package nerd.cave.web.endpoints.api

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import nerd.cave.service.checkin.CheckInService
import nerd.cave.service.member.MemberService
import nerd.cave.service.payment.PaymentService
import nerd.cave.store.StoreService
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.endpoints.api.action.MemberEndpoints
import nerd.cave.web.endpoints.api.branch.BranchEndpoints
import nerd.cave.web.endpoints.api.checkin.CheckInEndpoints
import nerd.cave.web.endpoints.api.disclaimer.DisclaimerEndpoints
import nerd.cave.web.endpoints.api.login.LoginEndpoints
import nerd.cave.web.endpoints.api.payment.PaymentEndpoints
import nerd.cave.web.endpoints.api.product.ProductEndpoints
import nerd.cave.web.endpoints.api.notification.WXNotificationEndpoints
import nerd.cave.web.session.NerdCaveSessionHandler
import nerd.cave.web.wx.WXWebClient
import nerd.cave.web.wx.payment.PaymentSecretRetriever
import nerd.cave.web.wx.payment.WXPayClient
import java.time.Clock

class ApiEndpoints (
    vertx: Vertx,
    clock: Clock,
    wxWebClient: WXWebClient,
    wxPayClient: WXPayClient,
    paymentSecretRetriever: PaymentSecretRetriever,
    storeService: StoreService,
    sessionHandler: NerdCaveSessionHandler,
    memberService: MemberService,
    paymentService: PaymentService,
    checkInService: CheckInService
): HttpEndpoint {

    override val router: Router = Router.router(vertx).apply {
        mountSubRouter("/login", LoginEndpoints(vertx, wxWebClient, storeService.sessionStoreService, storeService.memberStoreService, memberService).router)
        mountSubRouter("/notification", WXNotificationEndpoints(vertx, clock, paymentSecretRetriever, storeService.wxPaymentCallbackStoreService, paymentService).router)
        mountSubRouter("/member", MemberEndpoints(vertx, sessionHandler, memberService).router)
        mountSubRouter("/checkIn", CheckInEndpoints(vertx, sessionHandler, storeService, checkInService).router)
        mountSubRouter("/branch", BranchEndpoints(vertx, storeService.branchStoreService, sessionHandler).router)
        mountSubRouter("/product", ProductEndpoints(vertx, storeService.productStoreService, sessionHandler).router)
        mountSubRouter("/payment", PaymentEndpoints(vertx, clock, wxPayClient, sessionHandler, paymentSecretRetriever, memberService, paymentService, storeService.productStoreService).router)
        mountSubRouter("/disclaimer", DisclaimerEndpoints(vertx, storeService.disclaimerStoreService, sessionHandler).router)
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