package nerd.cave.web

import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.core.http.closeAwait
import io.vertx.kotlin.core.http.listenAwait
import nerd.cave.Environment
import nerd.cave.component.LifeCycle
import nerd.cave.service.branch.BranchService
import nerd.cave.service.checkin.CheckInService
import nerd.cave.service.holiday.HolidayService
import nerd.cave.service.member.MemberService
import nerd.cave.service.order.OrderService
import nerd.cave.store.StoreService
import nerd.cave.web.client.WebClient
import nerd.cave.web.endpoints.GreetingEndpoints
import nerd.cave.web.endpoints.admin.AdminEndpoints
import nerd.cave.web.endpoints.api.ApiEndpoints
import nerd.cave.web.endpoints.management.ManagementEndpoints
import nerd.cave.web.extentions.route
import nerd.cave.web.handlers.redirect
import nerd.cave.web.session.AdminAccountSessionHandlerImpl
import nerd.cave.web.session.NerdCaveSessionHandlerImpl
import nerd.cave.web.wx.WXWebClient
import nerd.cave.web.wx.config.WXConfig
import nerd.cave.web.wx.payment.PaymentSecretRetriever
import nerd.cave.web.wx.payment.WXPayClient
import org.slf4j.LoggerFactory
import java.time.Clock
import javax.inject.Inject

class WebServer @Inject constructor(
    private val environment: Environment,
    private val vertx: Vertx,
    private val clock: Clock,
    private val webClient: WebClient,
    private val storeService: StoreService,
    private val memberService: MemberService,
    private val checkInService: CheckInService,
    private val orderService: OrderService,
    private val branchService: BranchService,
    private val holidayService: HolidayService
): LifeCycle {
    companion object {
        private val logger = LoggerFactory.getLogger(WebServer::class.java)
    }

    private val wxConfig = WXConfig.forEnv(environment)
    private val paymentSecretRetriever = PaymentSecretRetriever.forEnv(environment, wxConfig, webClient)
    private val wxWebClient = WXWebClient(webClient, wxConfig)
    private val wxPayClient = WXPayClient(webClient, wxConfig, paymentSecretRetriever)
    lateinit var httpServer: HttpServer

    override suspend fun start() {
        logger.info("Running WebServer on $environment")
        val port = System.getProperty("server.port")?.toInt() ?: 8080
        val router = buildRouter()

        logger.info("Trying to start http server on port $port")
        httpServer = vertx.createHttpServer()
            .requestHandler(router)
            .listenAwait(port)

        logger.info("Started http server on port ${httpServer.actualPort()}")
    }

    override suspend fun stop() {
        logger.info("Stopping http server...")
        httpServer.closeAwait()
    }

    private fun buildRouter(): Router {
        val memberSessionHandler = NerdCaveSessionHandlerImpl(storeService.memberStoreService, storeService.sessionStoreService)
        val adminSessionHandler = AdminAccountSessionHandlerImpl(clock, storeService.adminAccountStoreService, storeService.adminSessionStoreService)
        return Router.router(vertx).apply {
            route().handler(BodyHandler.create())
            route("/", StaticHandler.create("web").setMaxAgeSeconds(0))
            route("/swagger/*", StaticHandler.create("webroot/swagger").setMaxAgeSeconds(0))
            route("/swagger", redirect("/swagger/"))
            mountSubRouter("/admin", AdminEndpoints(vertx, adminSessionHandler, clock, storeService, orderService, memberService, branchService, checkInService).router)
            mountSubRouter("/api", ApiEndpoints(vertx, clock, wxWebClient, wxPayClient, paymentSecretRetriever, storeService, memberSessionHandler, memberService, checkInService, orderService, branchService, holidayService).router)
            mountSubRouter("/greeting", GreetingEndpoints(vertx, wxConfig, webClient, wxPayClient).router)
            mountSubRouter("/mnt", ManagementEndpoints(vertx).router)
        }
    }
}