package nerd.cave

import io.vertx.core.Vertx
import nerd.cave.store.MongoStoreService
import nerd.cave.web.WebServer
import nerd.cave.web.client.WebClient
import java.util.concurrent.Executors

class NerdCaveServer(environment: Environment) {
    val vertx = Vertx.vertx()
    val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    val store = MongoStoreService(environment.mongoConfig)
    val webClient = WebClient(executor)
    val webServer = WebServer(vertx, webClient)

    suspend fun start(): Unit {
        store.start()
        webClient.start()
        webServer.start()
    }
}