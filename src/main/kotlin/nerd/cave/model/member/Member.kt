package nerd.cave.model.member

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDate
import java.time.ZonedDateTime

data class Member(
    val memberId: String,
    val memberType: MemberType,
    val memberDetail: MemberDetail,
    val memberSourceType: MemberSourceType,
    val memberSource: MemberSource,
    val registerTime: ZonedDateTime
)

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="memberType")
@JsonSubTypes(
    JsonSubTypes.Type(value = NormalMember::class, name = "NORMAL"),
    JsonSubTypes.Type(value = LimitedEntriesMember::class, name = "LIMITED_ENTRIES"),
    JsonSubTypes.Type(value = AnnualMember::class, name = "ANNUAL"),
    JsonSubTypes.Type(value = MonthlyMember::class, name = "MONTHLY")
)
interface MemberDetail

data class LimitedEntriesMember(
    val totalEntries: Int,
    val usedEntries: Int
): MemberDetail

data class AnnualMember (
    val startedDate: LocalDate,
    val expiredDate: LocalDate
): MemberDetail

data class MonthlyMember (
    val startedDate: LocalDate,
    val expiredDate: LocalDate
): MemberDetail

class NormalMember : MemberDetail

enum class MemberType {
    NORMAL,
    LIMITED_ENTRIES,
    MONTHLY,
    ANNUAL
}

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="memberType")
@JsonSubTypes(
    JsonSubTypes.Type(value = WechatMember::class, name = "WECHAT")
)
interface MemberSource

data class WechatMember (
    val openid: String
): MemberSource

enum class MemberSourceType {
    OFFLINE,
    WECHAT
}