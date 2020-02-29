package nerd.cave.model.token

import nerd.cave.util.CHECKIN_DATE_FORMMATER
import java.time.LocalDate
import java.time.ZonedDateTime

data class Token(
    val id: String,
    val branchId: String,
    val memberId: String,
    val hasEquipment: Boolean,
    val checkInNumber: String,
    val checkInTime: ZonedDateTime,
    val checkInDate: String
)

fun LocalDate.toTokenDateFormat(): String {
    return format(CHECKIN_DATE_FORMMATER)
}

fun String.toTokenLocalDate(): LocalDate {
    return LocalDate.parse(this, CHECKIN_DATE_FORMMATER)
}