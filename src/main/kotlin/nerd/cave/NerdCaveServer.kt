package nerd.cave

import io.vertx.core.Vertx
import nerd.cave.component.LifeCycle
import nerd.cave.service.checkin.CheckInNumberGeneratorMongoImpl
import nerd.cave.service.checkin.CheckInServiceImpl
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

    private val vertx = Vertx.vertx()
    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    private val clock = Clock.system(ZoneOffset.ofHours(8))
    private val storeService = MongoStoreService(MongoConfig.forEnv(environment), clock)
    private val paymentService = PaymentServiceImpl(storeService, clock)
    private val memberService = MemberServiceImpl(clock, storeService)
    private val checkInNumberGenerator = CheckInNumberGeneratorMongoImpl(storeService)
    private val checkInService = CheckInServiceImpl(clock, storeService, memberService, checkInNumberGenerator)
    private val webClient = WebClient(executor)
    private val webServer = WebServer(environment, vertx, clock, webClient, storeService, memberService, paymentService, checkInService)

    override suspend fun start(){
        logger.info("Starting $environment Nerdcave server ")
        storeService.start()
        webClient.start()
        webServer.start()
    }

    override suspend fun stop() {
        logger.info("Stopping Nerdcave server ")
        webServer.stop()
        webClient.stop()
        storeService.stop()
    }
}