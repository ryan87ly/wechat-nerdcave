package nerd.cave.web.endpoints.management

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.extentions.coroutine
import org.slf4j.LoggerFactory

class ManagementEndpoints(vertx: Vertx): HttpEndpoint {
    override val router = Router.router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        get("/ping") { ping(it) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ManagementEndpoints::class.java)
    }

    private fun ping(context: RoutingContext) {
        context.response().end("pong")
    }
}