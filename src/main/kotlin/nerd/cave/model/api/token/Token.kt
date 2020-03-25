package nerd.cave.model.api.token

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import nerd.cave.model.api.member.MemberType
import nerd.cave.util.LocalDateSerializer
import nerd.cave.util.ZonedDateTimeSerializer
import java.time.LocalDate
import java.time.ZonedDateTime

data class Token(
    val id: String,
    val branchId: String,
    val memberId: String,
    val memberType: MemberType,
    val hasEquipment: Boolean,
    val checkInNumber: String,
    val checkInTime: ZonedDateTime,
    val checkInDate: LocalDate
)

data class EnrichedToken(
    val branchId: String,
    val branchName: String,
    val memberId: String,
    val memberName: String,
    val memberType: MemberType,
    @get:JsonSerialize(using = ZonedDateTimeSerializer::class)
    val checkInTime: ZonedDateTime,
    @get:JsonSerialize(using = LocalDateSerializer::class)
    val checkInDate: LocalDate
)