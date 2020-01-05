package nerd.cave.web.endpoints

import io.vertx.core.Vertx
import io.vertx.ext.web.Router.router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.delay
import nerd.cave.web.client.WebClient
import nerd.cave.web.exceptions.ForbiddenException
import nerd.cave.web.extentions.coroutine
import nerd.cave.web.wx.config.WXConfig
import nerd.cave.web.wx.payment.WXPayClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class GreetingEndpoints(vertx: Vertx, private val wxConfig: WXConfig, webClient: WebClient, private val wxPayClient: WXPayClient): HttpEndpoint {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(GreetingEndpoints::class.java)
    }

    override val router = router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        get("/"){ greeting(it) }
        post("/:name") { greetingWithBody(it) }
        get("/exception") { doSomethingWrong(it) }
        get("/prepay/:openid") { prepay(it) }
        get("/sign") { sign(it) }
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

    private suspend fun prepay(ctx: RoutingContext) {
        val remoteHost = ctx.request().connection().remoteAddress().host()
        println("request host $remoteHost")
        val openid = ctx.request().params().get("openid")
        val response = wxPayClient.placeUnifiedOrder(openid, UUID.randomUUID().toString(), "test", 201, remoteHost)
        ctx.response().end(
            response.toString()
        )
    }

    private suspend fun sign(ctx: RoutingContext) {
        val queryParams = ctx.request().params()
        val signature = nerd.cave.web.wx.sign(wxConfig.paymentSecret, *(queryParams.entries().map { it.key to it.value }.toTypedArray()))
        ctx.response().end(
            signature
        )
    }

}