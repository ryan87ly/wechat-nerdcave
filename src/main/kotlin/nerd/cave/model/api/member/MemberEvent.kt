package nerd.cave.model.api.member

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import nerd.cave.util.LOCALDATE_FORMMATER
import java.time.LocalDate
import java.time.ZonedDateTime

data class MemberEvent(
    val memberId: String,
    val eventType: MemberEventType,
    val eventDetail: MemberEventDetail,
    val time: ZonedDateTime
)

enum class MemberEventType {
    Entries,
    YearlyMember,
    MonthlyMember
}

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="eventType")
@JsonSubTypes(
    JsonSubTypes.Type(value = EntriesEvent::class, name = "ENTRIES"),
    JsonSubTypes.Type(value = YearlyMemberEvent::class, name = "YEARLY"),
    JsonSubTypes.Type(value = MonthlyMemberEvent::class, name = "MONTHLY")
)
interface MemberEventDetail

interface ContractMemberEventDetail: MemberEventDetail {
    val startDate: LocalDate
    val expiryDate: LocalDate
}

data class EntriesEvent(
    val entryNumber: Int
): MemberEventDetail

data class YearlyMemberEvent (
    override val startDate: LocalDate,
    override val expiryDate: LocalDate
): ContractMemberEventDetail

data class MonthlyMemberEvent(
    override val startDate: LocalDate,
    override val expiryDate: LocalDate
): ContractMemberEventDetail