package nerd.cave.web.endpoints.api.disclaimer

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.disclaimer.DisclaimerSignature
import nerd.cave.store.DisclaimerStoreService
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.extentions.coroutine
import nerd.cave.web.extentions.getMandatoryString
import nerd.cave.web.extentions.ok
import nerd.cave.web.session.NerdCaveSessionHandler
import nerd.cave.web.session.nerdCaveMember
import org.slf4j.LoggerFactory

class DisclaimerEndpoints(vertx: Vertx, private val disclaimerStoreService: DisclaimerStoreService, sessionHandler: NerdCaveSessionHandler): HttpEndpoint {
    companion object {
        private val logger = LoggerFactory.getLogger(DisclaimerStoreService::class.java)
    }

    override val router: Router = Router.router(vertx).coroutine(vertx.dispatcher(), DisclaimerEndpoints.logger).apply {
        route(sessionHandler.handler)
        get("/hasSigned") { hasSignedDisclaimer(it) }
        post("/sign") { signDisclaimer(it) }
    }

    private suspend fun hasSignedDisclaimer(ctx: RoutingContext) {
        val member = ctx.nerdCaveMember()
        val signed = disclaimerStoreService.hasSignedDisclaimer(member.id)
        ctx.response().ok(
            jsonObjectOf(
                "signed" to signed
            )
        )
    }

    private suspend fun signDisclaimer(ctx: RoutingContext) {
        val member = ctx.nerdCaveMember()
        val body = ctx.bodyAsJson
        val legalName = body.getMandatoryString("legalName")
        val contactNumber = body.getMandatoryString("contactNumber")
        val emergentContactNumber = body.getMandatoryString("emergentContactNumber")
        val signature = DisclaimerSignature(member.id, legalName, contactNumber, emergentContactNumber)
        disclaimerStoreService.signDisclaimer(signature)
        ctx.response().ok(
            jsonObjectOf(
                "ok" to 1
            )
        )
    }
}