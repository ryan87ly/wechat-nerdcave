package nerd.cave

import io.vertx.core.Vertx
import nerd.cave.web.WebServer
import nerd.cave.web.client.WebClient
import java.util.concurrent.Executors

class NerdCaveServer {
    val vertx = Vertx.vertx()
    val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    val webClient = WebClient(executor)
    val webServer = WebServer(vertx, webClient)

    suspend fun start(): Unit {
        webClient.start()
        webServer.start()
    }
}