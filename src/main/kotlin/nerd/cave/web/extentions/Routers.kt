package nerd.cave.web.extentions

import io.vertx.core.Handler
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.CoroutineDispatcher
import nerd.cave.web.CoroutineRouter
import org.slf4j.Logger

fun Router.coroutine(dispatcher: CoroutineDispatcher, logger: Logger): CoroutineRouter {
    return CoroutineRouter(dispatcher, logger, this)
}

fun Router.route(path:String, handler: Handler<RoutingContext>): Route {
    return this.route(path).handler(handler)
}

//fun Router.redirect(target: String): Router