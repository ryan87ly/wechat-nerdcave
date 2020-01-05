package nerd.cave.web.handlers

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

fun redirect(target: String): Handler<RoutingContext> {
    return Handler { ctx ->
        ctx.response()
            .putHeader("location", target)
            .setStatusCode(HttpResponseStatus.FOUND.code())
            .end()
    }
}