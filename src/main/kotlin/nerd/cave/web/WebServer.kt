package nerd.cave.web

import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.http.listenAwait
import nerd.cave.web.endpoints.GreetingEndpoints

class WebServer(val vertx: Vertx) {
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
            mountSubRouter("/greeting", GreetingEndpoints(vertx).router)
        }
    }
}