package nerd.cave.web

import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.core.http.listenAwait
import nerd.cave.web.client.WebClient
import nerd.cave.web.endpoints.GreetingEndpoints
import nerd.cave.web.endpoints.api.ApiEndpoints
import nerd.cave.web.wx.WXWebClient

class WebServer(val vertx: Vertx, val webClient: WebClient) {
    private val wxWebClient = WXWebClient(webClient)
    lateinit var httpServer: HttpServer

    suspend fun start() {
        val port = System.getProperty("server.port")?.toInt() ?: 8080
        val router = buildRouter()

        println("Trying to start http server on port $port")
        httpServer = vertx.createHttpServer()
            .requestHandler(router)
            .listenAwait(port)

        println("Started http server on port ${httpServer.actualPort()}")
    }

    private fun buildRouter(): Router {
        return Router.router(vertx).apply {
            route().handler(BodyHandler.create())
            route("/swagger/*").handler(StaticHandler.create("webroot/swagger"))
            mountSubRouter("/api", ApiEndpoints(vertx, wxWebClient).router)
            mountSubRouter("/greeting", GreetingEndpoints(vertx, webClient).router)
        }
    }
}