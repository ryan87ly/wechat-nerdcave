package nerd.cave

import io.vertx.core.Vertx
import nerd.cave.web.WebServer

class MyServer {
    val vertx = Vertx.vertx()
    val webServer = WebServer(vertx)

    suspend fun start(): Unit {
        webServer.start()
    }
}