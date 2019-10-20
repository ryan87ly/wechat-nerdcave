package nerd.cave.web.endpoints

import io.vertx.core.Vertx
import io.vertx.ext.web.Router.router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.delay
import nerd.cave.web.exceptions.ForbiddenException
import nerd.cave.web.extentions.coroutine

class GreetingEndpoints(vertx: Vertx): HttpEndpoint {
    override val router = router(vertx).coroutine(vertx.dispatcher()).apply {
        get("/"){ greeting(it) }
        post("/:name") { greetingWithBody(it) }
        get("/exception") { doSomethingWrong(it) }
    }

    private fun greeting(ctx: RoutingContext) {
        ctx.response().end("Greeting from Vertx")
    }

    private suspend fun greetingWithBody(ctx: RoutingContext) {
        delay(100)
        val name = ctx.request().params().get("name")
        val msg = ctx.bodyAsJson.get<String>("msg")
        ctx.response().end(
            jsonObjectOf(
                "message" to "Greeting from $name: $msg"
            )
            .encodePrettily()
        )
    }

    private fun doSomethingWrong(ctx: RoutingContext) {
        throw ForbiddenException("not allowed")
    }

}