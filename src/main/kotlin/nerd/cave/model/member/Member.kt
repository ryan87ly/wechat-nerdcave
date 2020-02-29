package nerd.cave.model.member

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import nerd.cave.util.CHECKIN_DATE_FORMMATER
import java.time.LocalDate
import java.time.ZonedDateTime

data class Member(
    val id: String,
    val memberSourceType: MemberSourceType,
    val memberSource: MemberSource,
    val registerTime: ZonedDateTime
)

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="memberType")
@JsonSubTypes(
    JsonSubTypes.Type(value = WechatMember::class, name = "WECHAT")
)
interface MemberSource

data class WechatMember (
    val openid: String,
    val nickName: String,
    val gender: String
): MemberSource

enum class MemberSourceType {
    OFFLINE,
    WECHAT
}

interface MemberDetail {
    val memberType: MemberType
}

interface ContractMemberDetail: MemberDetail {
    @get:JsonSerialize(using = ExpiryDateSerializer::class)
    val expiryDate: LocalDate
}

class ExpiryDateSerializer: JsonSerializer<LocalDate>() {
    override fun serialize(value: LocalDate, gen: JsonGenerator, serializers: SerializerProvider?) {
        gen.writeString(value.format(CHECKIN_DATE_FORMMATER))
    }
}

data class YearlyMember (
    override val expiryDate: LocalDate,
    override val memberType: MemberType = MemberType.YEARLY
): ContractMemberDetail

data class MonthlyMember (
    override val expiryDate: LocalDate,
    override val memberType: MemberType = MemberType.MONTHLY
): ContractMemberDetail

data class NormalMember(
    val remainingEntries: Long,
    override val memberType: MemberType = MemberType.NORMAL
): MemberDetail

enum class MemberType {
    NORMAL,
    MONTHLY,
    YEARLY
}