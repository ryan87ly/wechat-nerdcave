package nerd.cave.web.endpoints.api.action

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.model.member.Member
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.exceptions.BadRequestException
import nerd.cave.web.extentions.coroutine
import nerd.cave.web.extentions.endIfOpen
import nerd.cave.web.session.NerdCaveSessionHandler
import nerd.cave.web.session.SESSION_MEMBER_KEY
import org.slf4j.LoggerFactory

class ActionEndpoints(vertx: Vertx, sessionHandler: NerdCaveSessionHandler): HttpEndpoint {
    companion object {
        private val logger = LoggerFactory.getLogger(ActionEndpoints::class.java)
    }

    override val router: Router = Router.router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        route(sessionHandler.handler)
        get("/memberInfo") { memberInfo(it) }
    }

    private fun memberInfo(ctx: RoutingContext) {
        ctx.get<Member>(SESSION_MEMBER_KEY)
            ?.let {
                ctx.response()
                    .endIfOpen(jsonObjectOf(
                        "memberType" to it.memberType,
                        "memberDetail" to it.memberDetail
                    ))
            } ?: throw BadRequestException("Unable to retrieve member from session")
    }

}