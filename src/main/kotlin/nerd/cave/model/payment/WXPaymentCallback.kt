package nerd.cave.model.payment

import java.time.ZonedDateTime

data class WXPaymentCallback (
    val timestamp: ZonedDateTime,
    val rawBody: String,
    val content: Map<String, Any>
)