package nerd.cave.model.api.session

import java.time.ZonedDateTime

const val SESSION_COOKIE_NAME = "session_id";

data class Session (
    val id: String,
    val memberId: String,
    val createTime: ZonedDateTime
)