package nerd.cave.web

import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nerd.cave.web.exceptions.HttpHandlerException
import kotlin.coroutines.CoroutineContext

class CoroutineRouter(val dispatcher: CoroutineDispatcher, router: Router): Router by router, CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = dispatcher

    fun get(
        path: String,
        handler: suspend (RoutingContext) -> Unit
    ): Route {
        return route(HttpMethod.GET, path, handler)
    }

    fun post(
        path: String,
        handler: suspend (RoutingContext) -> Unit
    ): Route {
        return route(HttpMethod.POST, path, handler)
    }

    fun route(
        method: HttpMethod,
        path: String,
        handler: suspend (RoutingContext) -> Unit
    ): Route {
        return route(method, path).handler { ctx ->
            launch {
                try {
                    handler(ctx)
                } catch (e: HttpHandlerException) {
                    println("HttpHandlerException in handler $e")
                    ctx.response().statusCode = e.statusCode
                    ctx.response().end(e.message)
                } catch (t: Throwable) {
                    println("Exception in handler $t")
                    ctx.response().statusCode = 500
                    ctx.response().end(t.message)
                }
            }
        }
    }
}