package nerd.cave.model.api.branch

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import nerd.cave.util.LocalTimeSerializer
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

data class Branch(
    val id: String,
    val name: String,
    val location: LocationInfo,
    val weekdayOpenHour: OpenHourInfo,
    val holidayOpenHour: OpenHourInfo,
    val contactNumbers: List<String>,
    val description: String,
    val active: Boolean
)

data class LocationInfo(
    val longitude: Double,
    val latitude: Double,
    val description: String
)

data class OpenHourInfo(
    @get:JsonSerialize(using = LocalTimeSerializer::class)
    val openTime: LocalTime,
    @get:JsonSerialize(using = LocalTimeSerializer::class)
    val closeTime: LocalTime,
    val description: String
)

data class BranchOpenStatus(
    val branchId: String,
    val date: LocalDate,
    val updatedTime: ZonedDateTime,
    val status: OpenStatus
)

enum class OpenStatus{
    OPEN,
    CLOSED
}

fun OpenHourInfo.isOpen(time: LocalTime): Boolean {
    return !time.isBefore(openTime) && time.isBefore(closeTime)
}