package nerd.cave.model.api.token

import java.time.LocalDate
import java.time.ZonedDateTime

data class Token(
    val id: String,
    val branchId: String,
    val memberId: String,
    val hasEquipment: Boolean,
    val checkInNumber: String,
    val checkInTime: ZonedDateTime,
    val checkInDate: LocalDate
)