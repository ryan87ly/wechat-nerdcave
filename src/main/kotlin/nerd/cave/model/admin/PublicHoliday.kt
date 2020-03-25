package nerd.cave.model.admin

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import nerd.cave.util.LocalDateSerializer
import java.time.LocalDate

data class PublicHoliday(
    @get:JsonSerialize(using = LocalDateSerializer::class)
    val date: LocalDate
)