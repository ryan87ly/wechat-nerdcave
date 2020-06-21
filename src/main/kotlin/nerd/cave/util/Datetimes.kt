package nerd.cave.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import nerd.cave.web.exceptions.BadRequestException
import java.time.*
import java.time.format.DateTimeFormatter

val LOCALDATE_FORMMATER = DateTimeFormatter.ofPattern("yyyyMMdd")
val LOCALDATETIME_FORMMATER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
val TIME_ZONE = ZoneOffset.ofHours(8)
val LOCALTIME_FORMMATER = DateTimeFormatter.ofPattern("HHmmss")
val LOCALTIME_SPREADSHEET_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")

fun String.toLocalDateTime(): LocalDateTime {
    try {
        return LocalDateTime.parse(this, LOCALDATETIME_FORMMATER)
    } catch (e: DateTimeException) {
        throw BadRequestException(e.message)
    }
}

fun LocalDateTime.toFormattedString(): String {
    return this.format(LOCALDATETIME_FORMMATER)
}

fun LocalTime.toSpreadSheetString(): String {
    return this.format(LOCALTIME_SPREADSHEET_FORMATTER)
}

fun LocalDate.toFormattedString(): String {
    return this.format(LOCALDATE_FORMMATER)
}

fun ZonedDateTime.toFormattedString(): String {
    return this.withZoneSameInstant(TIME_ZONE).format(LOCALDATETIME_FORMMATER)
}

fun String.toLocalDate(): LocalDate {
    try {
        return LocalDate.parse(this, LOCALDATE_FORMMATER)
    } catch (e: DateTimeException) {
        throw BadRequestException(e.message)
    }
}

class LocalDateSerializer: JsonSerializer<LocalDate>() {
    override fun serialize(value: LocalDate, gen: JsonGenerator, serializers: SerializerProvider?) {
        gen.writeString(value.format(LOCALDATE_FORMMATER))
    }
}

class LocalDateTimeSerializer: JsonSerializer<LocalDateTime>() {
    override fun serialize(value: LocalDateTime, gen: JsonGenerator, serializers: SerializerProvider?) {
        gen.writeString(value.format(LOCALDATETIME_FORMMATER))
    }
}

class LocalTimeSerializer: JsonSerializer<LocalTime>() {
    override fun serialize(value: LocalTime, gen: JsonGenerator, serializers: SerializerProvider?) {
        gen.writeString(value.format(LOCALTIME_FORMMATER))
    }
}

class ZonedDateTimeSerializer: JsonSerializer<ZonedDateTime>() {
    override fun serialize(value: ZonedDateTime, gen: JsonGenerator, serializers: SerializerProvider?) {
        gen.writeString(value.withZoneSameInstant(TIME_ZONE).format(LOCALDATETIME_FORMMATER))
    }
}

fun LocalDate.isWeekend(): Boolean {
    return this.dayOfWeek == DayOfWeek.SATURDAY || this.dayOfWeek == DayOfWeek.SUNDAY
}