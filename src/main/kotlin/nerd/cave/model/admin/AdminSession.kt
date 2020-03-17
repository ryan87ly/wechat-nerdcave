package nerd.cave.model.admin

import java.time.ZonedDateTime

const val ADMIN_SESSION_COOKIE_NAME = "admin_session_id"
const val VALID_TIME_SECONDS: Long = 60 * 60

data class AdminSession(
    val id: String,
    val accountId: String,
    val createTime: ZonedDateTime,
    val expiryTime: ZonedDateTime,
    val active: Boolean
)
