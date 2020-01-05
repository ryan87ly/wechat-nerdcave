package nerd.cave.web.endpoints.api.login

import io.vertx.core.Vertx
import io.vertx.core.http.Cookie
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.model.session.SESSION_COOKIE_NAME
import nerd.cave.store.MemberStoreService
import nerd.cave.store.SessionStoreService
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.exceptions.BadRequestException
import nerd.cave.web.exceptions.ForbiddenException
import nerd.cave.web.extentions.coroutine
import nerd.cave.web.extentions.endIfOpen
import nerd.cave.web.wx.WXWebClient
import nerd.cave.web.wx.isSuccess
import org.slf4j.LoggerFactory

class LoginEndpoints(
    vertx: Vertx,
    private val wxWebClient: WXWebClient,
    private val sessionStoreService: SessionStoreService,
    private val memberStoreService: MemberStoreService): HttpEndpoint {

    companion object {
        private val logger = LoggerFactory.getLogger(LoginEndpoints::class.java)
    }

    override val router = Router.router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        post("/") { userLogin(it) }
    }


    private suspend fun userLogin(ctx: RoutingContext) {
        val code = ctx.bodyAsJson.getString("code") ?: throw ForbiddenException("Empty code param")
        logger.info("Login code $code")
        val requestResponse = wxWebClient.code2Session(code)
        if (!requestResponse.isSuccess()) {
            throw BadRequestException("Error when calling WX api, code: ${requestResponse.getInteger("errcode")}, msg: ${requestResponse.getString("errmsg")}")
        } else {
            val openid = requestResponse.getString("openid")
            logger.info("code: [$code], openid: [$openid]")
            val member = memberStoreService.getOrCreateWechatMember(openid)
            val session = sessionStoreService.newSession(member.memberId)
            ctx.addCookie(Cookie.cookie(SESSION_COOKIE_NAME, session.id))
                .response()
                .endIfOpen(jsonObjectOf(
                    "memberType" to member.memberType,
                    "memberDetail" to member.memberDetail
                )
            )
        }
    }
}