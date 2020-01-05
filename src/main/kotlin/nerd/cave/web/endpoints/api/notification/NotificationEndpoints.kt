package nerd.cave.web.endpoints.notification

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.extentions.coroutine
import org.slf4j.LoggerFactory


class NotificationEndpoints(vertx: Vertx): HttpEndpoint {
    companion object {
        private val logger = LoggerFactory.getLogger(NotificationEndpoints::class.java)
    }

    override val router = Router.router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        post("/payment") { paymentCallback(it) }
    }

    private fun paymentCallback(context: RoutingContext) {
        logger.info("payment callback called, body ${context.bodyAsString}")
    }
}