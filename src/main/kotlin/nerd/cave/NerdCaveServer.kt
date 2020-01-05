package nerd.cave

import io.vertx.core.Vertx
import nerd.cave.component.LifeCycle
import nerd.cave.service.member.MemberServiceImpl
import nerd.cave.service.payment.PaymentServiceImpl
import nerd.cave.store.config.MongoConfig
import nerd.cave.store.mongo.MongoStoreService
import nerd.cave.web.WebServer
import nerd.cave.web.client.WebClient
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.ZoneOffset
import java.util.concurrent.Executors

class NerdCaveServer(private val environment: Environment): LifeCycle {
    companion object {
        private val logger = LoggerFactory.getLogger(NerdCaveServer::class.java)
    }

    val vertx = Vertx.vertx()
    val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    val clock = Clock.system(ZoneOffset.ofHours(8))
    val store = MongoStoreService(MongoConfig.forEnv(environment), clock)
    val paymentService = PaymentServiceImpl(store)
    val memberService = MemberServiceImpl(clock)
    val webClient = WebClient(executor)
    val webServer = WebServer(environment, vertx, clock, webClient, store, memberService, paymentService)

    override suspend fun start(){
        logger.info("Starting $environment Nerdcave server ")
        store.start()
        webClient.start()
        webServer.start()
    }

    override suspend fun stop() {
        logger.info("Stopping Nerdcave server ")
        webServer.stop()
        webClient.stop()
        store.stop()
    }
}