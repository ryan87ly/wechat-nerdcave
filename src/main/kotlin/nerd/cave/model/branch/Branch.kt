package nerd.cave.model.branch

import java.time.LocalTime

data class Branch (
    val id:String,
    val location: LocationInfo,
    val openHour: OpenHourInfo,
    val contactNumbers: List<String>,
    val description: String
)

data class LocationInfo (
    val longitude: Double,
    val latitude: Double,
    val description: String
)

data class OpenHourInfo (
    val openTime: LocalTime,
    val closeTime: LocalTime,
    val description: String
)