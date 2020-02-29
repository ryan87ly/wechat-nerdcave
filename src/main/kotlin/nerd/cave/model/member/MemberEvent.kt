package nerd.cave.model.member

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
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

data class EntriesEvent(
    val entryNumber: Int
): MemberEventDetail

data class YearlyMemberEvent (
    val year: Int
): MemberEventDetail

data class MonthlyMemberEvent(
    val month: Int
): MemberEventDetail



