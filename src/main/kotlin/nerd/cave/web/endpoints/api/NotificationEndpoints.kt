package nerd.cave.web.endpoints.api

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.json.JsonObject.mapFrom
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.store.StoreService
import nerd.cave.util.toFormattedString
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.exceptions.ResourceNotFoundException
import nerd.cave.web.extentions.coroutine
import nerd.cave.web.extentions.ok
import nerd.cave.web.session.NerdCaveSessionHandler
import org.slf4j.LoggerFactory

class NotificationEndpoints(
    vertx: Vertx,
    sessionHandler: NerdCaveSessionHandler,
    storeService: StoreService
): HttpEndpoint {
    companion object {
        private val logger = LoggerFactory.getLogger(NotificationEndpoints::class.java)
    }

    private val notificationStoreService = storeService.notificationStoreService

    override val router: Router = Router.router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        route(sessionHandler.handler)
        get("/latest") { latestNotification(it) }
    }

    private suspend fun latestNotification(ctx: RoutingContext) {
        val notification = notificationStoreService.latestNotification() ?: throw ResourceNotFoundException("No notification found")
        ctx.response().ok(
            jsonObjectOf(
                "title" to notification.title,
                "detail" to notification.detail,
                "time" to notification.time.toFormattedString()
            )
        )
    }
}