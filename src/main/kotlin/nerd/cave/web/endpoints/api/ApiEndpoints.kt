package nerd.cave.web.endpoints.api

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import nerd.cave.service.branch.BranchService
import nerd.cave.service.checkin.CheckInService
import nerd.cave.service.holiday.HolidayService
import nerd.cave.service.member.MemberService
import nerd.cave.service.order.OrderService
import nerd.cave.store.StoreService
import nerd.cave.web.endpoints.HttpEndpoint
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
    checkInService: CheckInService,
    orderService: OrderService,
    branchService: BranchService,
    holidayService: HolidayService
): HttpEndpoint {

    override val router: Router = Router.router(vertx).apply {
        mountSubRouter("/login", LoginEndpoints(vertx, wxWebClient, storeService, memberService).router)
        mountSubRouter("/notification", WXNotificationEndpoints(vertx, clock, paymentSecretRetriever, storeService, orderService).router)
        mountSubRouter("/member", MemberEndpoints(vertx, sessionHandler, memberService).router)
        mountSubRouter("/checkIn", CheckInEndpoints(vertx, sessionHandler, storeService, checkInService).router)
        mountSubRouter("/branch", BranchEndpoints(vertx, sessionHandler, branchService).router)
        mountSubRouter("/product", ProductEndpoints(vertx, sessionHandler, clock, storeService, holidayService).router)
        mountSubRouter("/payment", PaymentEndpoints(vertx, clock, wxPayClient, sessionHandler, paymentSecretRetriever, memberService, orderService, branchService, storeService).router)
        mountSubRouter("/disclaimer", DisclaimerEndpoints(vertx, sessionHandler, storeService).router)
        mountSubRouter("/offlineorder", OfflineOrderEndpoints(vertx, sessionHandler, storeService, orderService).router)
        mountSubRouter("/notification", NotificationEndpoints(vertx, sessionHandler, storeService).router)
        mountSubRouter("/holiday", HolidayEndpoints(vertx, sessionHandler, holidayService).router)
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