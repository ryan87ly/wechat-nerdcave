package nerd.cave.web.endpoints.api

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.exceptions.ForbiddenException
import nerd.cave.web.extentions.coroutine
import nerd.cave.web.wx.WXWebClient

class UserEndpoints(vertx: Vertx, val wxWebClient: WXWebClient): HttpEndpoint {
    override val router = Router.router(vertx).coroutine(vertx.dispatcher()).apply {
        post("/login/:code") { userLogin(it) }
    }

    private suspend fun userLogin(ctx: RoutingContext) {
        val code = ctx.pathParams()["code"] ?: throw ForbiddenException("Empty code param")
        println("Code $code")
        val userInfo = wxWebClient.code2Session(code)
        println("userInfo $userInfo")
        ctx.response().end(userInfo.encodePrettily())
    }
}