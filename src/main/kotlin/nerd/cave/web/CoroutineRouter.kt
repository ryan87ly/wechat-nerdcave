package nerd.cave.web

import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nerd.cave.web.exceptions.HttpHandlerException
import nerd.cave.web.exceptions.InternalServerErrorException
import nerd.cave.web.extentions.endIfOpen
import org.slf4j.Logger
import kotlin.coroutines.CoroutineContext

class CoroutineRouter(private val dispatcher: CoroutineDispatcher, private val logger: Logger, router: Router): Router by router, CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = dispatcher

    fun get(
        path: String,
        handler: suspend (RoutingContext) -> Unit
    ): Route {
        return route(HttpMethod.GET, path, handler)
    }

    fun delete(
        path: String,
        handler: suspend (RoutingContext) -> Unit
    ): Route {
        return route(HttpMethod.DELETE, path, handler)
    }

    fun put(
        path: String,
        handler: suspend (RoutingContext) -> Unit
    ): Route {
        return route(HttpMethod.PUT, path, handler)
    }

    fun post(
        path: String,
        handler: suspend (RoutingContext) -> Unit
    ): Route {
        return route(HttpMethod.POST, path, handler)
    }

    fun route(
        handler: suspend (RoutingContext) -> Unit
    ): Route {
        return handle(route(), handler)
    }

    fun route(
        method: HttpMethod,
        path: String,
        handler: suspend (RoutingContext) -> Unit
    ): Route {
        return handle(route(method, path), handler)
    }

    private fun handle(route: Route, handler: suspend (RoutingContext) -> Unit): Route {
        return route.handler { ctx ->
            launch {
                try {
                    handler(ctx)
                } catch (e: HttpHandlerException) {
                    logger.warn("HttpHandlerException in handler $e")
                    ctx.response().endIfOpen(e)
                } catch (t: Throwable) {
                    logger.error("Exception in handler $t", t)
                    ctx.response().endIfOpen(InternalServerErrorException(t.message))
                }
            }
        }
    }
}