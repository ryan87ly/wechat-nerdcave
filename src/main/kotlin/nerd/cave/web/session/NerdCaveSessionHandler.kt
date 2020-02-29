package nerd.cave.web.session

import io.vertx.ext.web.RoutingContext
import nerd.cave.model.member.Member
import nerd.cave.model.session.SESSION_COOKIE_NAME
import nerd.cave.store.MemberStoreService
import nerd.cave.store.SessionStoreService
import nerd.cave.web.exceptions.BadRequestException
import nerd.cave.web.exceptions.UnauthorizedException
import org.slf4j.LoggerFactory

const val SESSION_MEMBER_KEY = "NERDCAVE_MEMBER"

class NerdCaveSessionHandlerImpl(private val memberStoreService: MemberStoreService, private val  sessionStoreService: SessionStoreService): NerdCaveSessionHandler {
    companion object {
        private val logger = LoggerFactory.getLogger(NerdCaveSessionHandlerImpl::class.java)
    }

    override val handler: suspend (RoutingContext) -> Unit
        get() = { ctx ->
            val sessionCookie = ctx.getCookie(SESSION_COOKIE_NAME)
                ?: throw UnauthorizedException("NO $SESSION_COOKIE_NAME found in cookie. Please call login api to retrieve valid session")
            val sessionId = sessionCookie.value
            val memberId = sessionStoreService.retrieveSession(sessionId)?.memberId
                ?: throw UnauthorizedException("Session id is not valid. Please call login api to retrieve valid session")
            logger.debug("member[$memberId] is make [${ctx.request().method()}] on ${ctx.request().path()}")
            memberStoreService.findMember(memberId)
                ?.let {
                    ctx.put(SESSION_MEMBER_KEY, it)
                    ctx.next()
                }
                ?: throw UnauthorizedException("Unable to retrieve valid member info. Please call login api to retrieve valid session")

        }
}

fun RoutingContext.nerdCaveMember(): Member {
    return this.get<Member>(SESSION_MEMBER_KEY) ?: throw BadRequestException("Unable to find session, please login")
}

interface NerdCaveSessionHandler {
    val handler: suspend (RoutingContext) -> Unit
}
