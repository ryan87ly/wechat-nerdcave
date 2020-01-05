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
import nerd.cave.service.member.MemberService
import nerd.cave.service.payment.PaymentService
import nerd.cave.store.StoreService
import nerd.cave.web.client.WebClient
import nerd.cave.web.endpoints.GreetingEndpoints
import nerd.cave.web.endpoints.api.ApiEndpoints
import nerd.cave.web.endpoints.management.ManagementEndpoints
import nerd.cave.web.extentions.route
import nerd.cave.web.handlers.redirect
import nerd.cave.web.session.NerdCaveSessionHandlerImpl
import nerd.cave.web.wx.WXWebClient
import nerd.cave.web.wx.config.WXConfig
import nerd.cave.web.wx.payment.PaymentSecretRetriever
import nerd.cave.web.wx.payment.WXPayClient
import org.slf4j.LoggerFactory
import java.time.Clock

class WebServer(
    environment: Environment,
    private val vertx: Vertx,
    private val clock: Clock,
    private val webClient: WebClient,
    private val storeService: StoreService,
    private val memberService: MemberService,
    private val paymentService: PaymentService
): LifeCycle {
    private val wxConfig = WXConfig.forEnv(environment)
    private val paymentSecretRetriever = PaymentSecretRetriever.forEnv(environment, wxConfig, webClient)
    private val wxWebClient = WXWebClient(webClient, wxConfig)
    private val wxPayClient = WXPayClient(webClient, wxConfig, paymentSecretRetriever)
    lateinit var httpServer: HttpServer

    companion object {
        val logger = LoggerFactory.getLogger(WebServer::class.java)
    }

    override suspend fun start() {
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
        val sessionHandler = NerdCaveSessionHandlerImpl(storeService.memberStoreService, storeService.sessionStoreService)
        return Router.router(vertx).apply {
            route().handler(BodyHandler.create())
            route("/swagger/*", StaticHandler.create("webroot/swagger"))
            route("/swagger", redirect("/swagger/"))
            mountSubRouter("/api", ApiEndpoints(vertx, clock, wxWebClient, wxPayClient, storeService, sessionHandler, memberService, paymentService).router)
            mountSubRouter("/greeting", GreetingEndpoints(vertx, wxConfig, webClient, wxPayClient).router)
            mountSubRouter("/mnt", ManagementEndpoints(vertx).router)
        }
    }
}