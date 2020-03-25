package nerd.cave.model.api.notification

import java.time.ZonedDateTime

data class Notification(
    val id: String,
    val title: String,
    val detail: String,
    val time: ZonedDateTime
)