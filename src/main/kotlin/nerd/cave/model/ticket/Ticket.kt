package nerd.cave.model.ticket

import java.time.ZonedDateTime

data class Ticket(
    val id: String,
    val paymentId: String,
    val memberId: String,
    val purchaseTime: ZonedDateTime,
    val hasEquipment: Boolean,
    val status: TicketStatus,
    val tokenId: String?
)

enum class TicketStatus {
    NOT_USED,
    USED
}