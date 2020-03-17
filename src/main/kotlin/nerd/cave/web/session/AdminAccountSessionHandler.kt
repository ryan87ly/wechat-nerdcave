package nerd.cave.web.session

import io.vertx.ext.web.RoutingContext
import nerd.cave.model.admin.ADMIN_SESSION_COOKIE_NAME
import nerd.cave.model.admin.Account
import nerd.cave.model.admin.AdminSession
import nerd.cave.model.admin.VALID_TIME_SECONDS
import nerd.cave.store.AdminAccountStoreService
import nerd.cave.store.AdminSessionStoreService
import nerd.cave.web.exceptions.UnauthorizedException
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.ZonedDateTime

const val SESSION_ADMIN_KEY = "ADMIN_ACCOUNT"

class AdminAccountSessionHandlerImpl(
    private val clock: Clock,
    private val adminAccountStoreService: AdminAccountStoreService,
    private val adminSessionStoreService: AdminSessionStoreService
): AdminAccountSessionHandler {
    companion object {
        private val logger = LoggerFactory.getLogger(NerdCaveSessionHandlerImpl::class.java)
    }

    override val handler: suspend (RoutingContext) -> Unit
        get() = { ctx ->
            val sessionCookie = ctx.getCookie(ADMIN_SESSION_COOKIE_NAME)
                ?: throw UnauthorizedException("NO $ADMIN_SESSION_COOKIE_NAME found in cookie. Please call login api to retrieve valid session")
            val sessionId = sessionCookie.value
            val session = adminSessionStoreService.retrieveSession(sessionId) ?: throw UnauthorizedException("Session id is not valid. Please call login api to retrieve valid session")
            if (!session.active && !session.isValid()) throw UnauthorizedException("Session is expired, please login again")
            adminSessionStoreService.renewSession(session.id, VALID_TIME_SECONDS)
            val accountId = session.accountId
            logger.debug("Account[${accountId}] is make [${ctx.request().method()}] on ${ctx.request().path()}")
            adminAccountStoreService.findById(accountId)
                ?.let {
                    ctx.put(SESSION_ADMIN_KEY, it)
                    ctx.next()
                }
                ?: throw UnauthorizedException("Unable to retrieve valid account info [$accountId].")
        }

    private fun AdminSession.isValid(): Boolean {
        val now = ZonedDateTime.now(clock)
        return this.expiryTime.isAfter(now)
    }
}

fun RoutingContext.adminAccount(): Account {
    return this.get<Account>(SESSION_ADMIN_KEY) ?: throw UnauthorizedException("Unable to find session, please login")
}

interface AdminAccountSessionHandler {
    val handler: suspend (RoutingContext) -> Unit
}