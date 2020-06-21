package nerd.cave.model.api.member

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.vertx.core.json.JsonObject
import nerd.cave.util.LOCALDATE_FORMMATER
import nerd.cave.util.LocalDateSerializer
import java.time.LocalDate
import java.time.ZonedDateTime

data class Member(
    val id: String,
    val memberSourceType: MemberSourceType,
    val memberSource: MemberSource,
    val memberDetail: MemberDetail,
    val registerTime: ZonedDateTime,
    val memberContact: MemberContact? = null
)

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="memberSourceType")
@JsonSubTypes(
    JsonSubTypes.Type(value = WechatMember::class, name = "WECHAT")
)
interface MemberSource

data class WechatMember (
    val openid: String,
    val nickName: String,
    val gender: Int
): MemberSource

enum class MemberSourceType {
    WECHAT
}

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="memberType")
@JsonSubTypes(
    JsonSubTypes.Type(value = NormalMember::class, name = "NORMAL"),
    JsonSubTypes.Type(value = YearlyMember::class, name = "YEARLY"),
    JsonSubTypes.Type(value = MonthlyMember::class, name = "MONTHLY"),
    JsonSubTypes.Type(value = MultiEntriesMember::class, name = "MULTI_ENTRIES")
)
interface MemberDetail {
    val memberType: MemberType
}

interface SpecialMemberDetail: MemberDetail {
    @get:JsonSerialize(using = LocalDateSerializer::class)
    val validFrom: LocalDate
    @get:JsonSerialize(using = LocalDateSerializer::class)
    val validUntil: LocalDate
}

interface UnlimitedEntriesMemberDetail: SpecialMemberDetail

data class YearlyMember (
    override val validFrom:LocalDate,
    override val validUntil: LocalDate,
    override val memberType: MemberType = MemberType.YEARLY
): UnlimitedEntriesMemberDetail

data class MonthlyMember (
    override val validFrom:LocalDate,
    override val validUntil: LocalDate,
    override val memberType: MemberType = MemberType.MONTHLY
): UnlimitedEntriesMemberDetail

data class MultiEntriesMember (
    val usedEntries: Int,
    val totalEntries: Int,
    override val validFrom: LocalDate,
    override val validUntil: LocalDate,
    override val memberType: MemberType = MemberType.MULTI_ENTRIES
): SpecialMemberDetail

fun SpecialMemberDetail.isExpired(date: LocalDate): Boolean {
    return date.isAfter(validUntil)
}

data class NormalMember(
    val remainingEntries: Long = 0,
    override val memberType: MemberType = MemberType.NORMAL
): MemberDetail

enum class MemberType {
    NORMAL,
    MULTI_ENTRIES,
    MONTHLY,
    YEARLY
}

fun MemberDetail.toJson(): JsonObject {
    return when(val m = this) {
        is MultiEntriesMember -> JsonObject.mapFrom(this).apply { put("remainingEntries", m.totalEntries - m.usedEntries) }
        else -> JsonObject.mapFrom(this)
    }
}

data class MemberContact (
    val legalName: String,
    val contactNumber: String,
    val emergentContactNumber: String
)