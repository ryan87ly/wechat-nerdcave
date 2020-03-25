package nerd.cave.web.endpoints.api

import io.vertx.core.Vertx
import io.vertx.core.http.Cookie
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.model.api.member.toJson
import nerd.cave.model.api.session.SESSION_COOKIE_NAME
import nerd.cave.service.member.MemberService
import nerd.cave.store.MemberStoreService
import nerd.cave.store.SessionStoreService
import nerd.cave.store.StoreService
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.exceptions.BadRequestException
import nerd.cave.web.extentions.coroutine
import nerd.cave.web.extentions.getMandatoryInt
import nerd.cave.web.extentions.getMandatoryString
import nerd.cave.web.extentions.ok
import nerd.cave.web.wx.WXWebClient
import nerd.cave.web.wx.isSuccess
import org.slf4j.LoggerFactory

class LoginEndpoints(
    vertx: Vertx,
    private val wxWebClient: WXWebClient,
    storeService: StoreService,
    private val memberService: MemberService
): HttpEndpoint {

    companion object {
        private val logger = LoggerFactory.getLogger(LoginEndpoints::class.java)
    }

    private val sessionStoreService: SessionStoreService by lazy { storeService.sessionStoreService }
    private val memberStoreService: MemberStoreService by lazy { storeService.memberStoreService }

    override val router = Router.router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        post("/") { userLogin(it) }
    }

    private suspend fun userLogin(ctx: RoutingContext) {
        val code = ctx.bodyAsJson.getMandatoryString("code")
        val nickName = ctx.bodyAsJson.getMandatoryString("nickName")
        val gender = ctx.bodyAsJson.getMandatoryInt("gender")
        logger.info("Login code $code")
        val requestResponse = wxWebClient.code2Session(code)
        if (!requestResponse.isSuccess()) {
            throw BadRequestException("Error when calling WX api, code: ${requestResponse.getInteger("errcode")}, msg: ${requestResponse.getString("errmsg")}")
        } else {
            val openid = requestResponse.getString("openid")
            logger.info("code: [$code], openid: [$openid]")
            val member = memberStoreService.getOrCreateWechatMember(openid, nickName, gender)
            val session = sessionStoreService.newSession(member.id)
            val memberDetail = memberService.getEffectiveMemberDetail(member)
            ctx.addCookie(Cookie.cookie(SESSION_COOKIE_NAME, session.id))
                .response()
                .ok(jsonObjectOf(
                    "memberType" to memberDetail.memberType,
                    "memberDetail" to memberDetail.toJson()
                )
            )
        }
    }
}