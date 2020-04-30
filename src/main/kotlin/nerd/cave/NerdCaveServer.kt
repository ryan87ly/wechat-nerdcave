package nerd.cave

import com.google.inject.Guice
import com.google.inject.Stage
import nerd.cave.component.LifeCycle
import nerd.cave.core.BasicModule
import nerd.cave.core.ServiceModule
import nerd.cave.core.StoreModule
import nerd.cave.store.StoreService
import nerd.cave.web.WebServer
import nerd.cave.web.client.WebClient
import org.slf4j.LoggerFactory
import javax.inject.Inject

class NerdCaveServer @Inject constructor(
    private val environment: Environment,
    private val storeService: StoreService,
    private val webClient: WebClient,
    private val webServer: WebServer
): LifeCycle {
    companion object {
        private val logger = LoggerFactory.getLogger(NerdCaveServer::class.java)

        fun create(environment: Environment): NerdCaveServer {
            val injector = Guice.createInjector(
                Stage.PRODUCTION,
                BasicModule(environment),
                ServiceModule(),
                StoreModule(environment)
            )
            return injector.getInstance(NerdCaveServer::class.java)
        }
    }

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