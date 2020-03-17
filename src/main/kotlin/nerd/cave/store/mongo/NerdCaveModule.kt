package nerd.cave.store.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import nerd.cave.util.LOCALDATETIME_FORMMATER
import nerd.cave.util.LOCALDATE_FORMMATER
import nerd.cave.util.LOCALTIME_FORMMATER
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object NerdCaveModule: SimpleModule() {
    init {
        addSerializer(LocalDate::class.java, LocalDateSerializer)
        addDeserializer(LocalDate::class.java, LocalDateDeserializer)
        addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer)
        addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer)
        addSerializer(LocalTime::class.java, LocalTimeSerializer)
        addDeserializer(LocalTime::class.java, LocalTimeDeserializer)
    }

    object LocalDateSerializer: JsonSerializer<LocalDate>() {
        override fun serialize(value: LocalDate, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeString(value.format(LOCALDATE_FORMMATER))
        }
    }

    object LocalDateDeserializer: JsonDeserializer<LocalDate>() {
        override fun deserialize(p: JsonParser, ctx: DeserializationContext): LocalDate {
            return LocalDate.parse(p.valueAsString, LOCALDATE_FORMMATER)
        }
    }

    object LocalDateTimeSerializer: JsonSerializer<LocalDateTime>() {
        override fun serialize(value: LocalDateTime, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeString(value.format(LOCALDATETIME_FORMMATER))
        }
    }

    object LocalDateTimeDeserializer: JsonDeserializer<LocalDateTime>() {
        override fun deserialize(p: JsonParser, ctx: DeserializationContext): LocalDateTime {
            return LocalDateTime.parse(p.valueAsString, LOCALDATETIME_FORMMATER)
        }
    }

    object LocalTimeSerializer: JsonSerializer<LocalTime>() {
        override fun serialize(value: LocalTime, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeString(value.format(LOCALTIME_FORMMATER))
        }
    }

    object LocalTimeDeserializer: JsonDeserializer<LocalTime>() {
        override fun deserialize(p: JsonParser, ctx: DeserializationContext): LocalTime {
            return LocalTime.parse(p.valueAsString, LOCALTIME_FORMMATER)
        }
    }
}
