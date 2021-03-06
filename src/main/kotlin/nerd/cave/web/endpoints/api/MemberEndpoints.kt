package nerd.cave.web.endpoints.api

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.json.JsonObject.mapFrom
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.model.api.member.MemberDetail
import nerd.cave.model.api.member.MultiEntriesMember
import nerd.cave.model.api.member.toJson
import nerd.cave.service.member.MemberService
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.extentions.coroutine
import nerd.cave.web.extentions.ok
import nerd.cave.web.session.NerdCaveSessionHandler
import nerd.cave.web.session.nerdCaveMember
import org.slf4j.LoggerFactory

class MemberEndpoints(
    vertx: Vertx,
    sessionHandler: NerdCaveSessionHandler,
    private val memberService: MemberService
): HttpEndpoint {
    companion object {
        private val logger = LoggerFactory.getLogger(MemberEndpoints::class.java)
    }
    override val router: Router = Router.router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        route(sessionHandler.handler)
        get("/info") { memberInfo(it) }
    }

    private suspend fun memberInfo(ctx: RoutingContext) {
        val member = ctx.nerdCaveMember()
        val memberDetail = memberService.getEffectiveMemberDetail(member)
        ctx.response()
            .ok(jsonObjectOf(
                "memberType" to memberDetail.memberType,
                "memberDetail" to memberDetail.toJson()
            ))
    }

}