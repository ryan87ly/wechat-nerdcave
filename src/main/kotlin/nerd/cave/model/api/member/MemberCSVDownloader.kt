package nerd.cave.model.api.member

import nerd.cave.util.CSVWriter
import nerd.cave.util.toFormattedString
import java.time.LocalDate

class MemberCSVDownloader(private val members: List<Member>) {
    private val headers = linkedMapOf(
        "wechatNickName" to "微信名称",
        "legalName" to "姓名",
        "contactNumber" to "电话",
        "memberType" to "会员",
        "remainingEntries" to "次数",
        "validPeriod" to "有效期",
        "emergentContactNumber" to "紧急联系人电话"
    )

    fun toCSVString(): String {
        return CSVWriter(headers)
            .addRows( *members.map { it.toMap() }.toTypedArray() )
            .toCSVString()
    }

    private fun Member.toMap(): Map<String, String?> {
        return mapOf(
            "wechatNickName" to when(val memberSource = this.memberSource) {
                is WechatMember -> memberSource.nickName
                else -> null
            },
            "legalName" to this.memberContact?.legalName,
            "contactNumber" to this.memberContact?.contactNumber,
            "emergentContactNumber" to this.memberContact?.emergentContactNumber,
            "memberType" to this.memberDetail.memberType.toDisplayName(),
            "remainingEntries" to when(val memberDetail = this.memberDetail) {
                is MultiEntriesMember -> (memberDetail.totalEntries - memberDetail.usedEntries).toString()
                else -> null
            },
            "validPeriod" to when(val memberDetail = this.memberDetail) {
                is SpecialMemberDetail -> toValidPeriod(memberDetail.validFrom, memberDetail.validUntil)
                else -> null
            }
        )
    }

    private fun MemberType.toDisplayName(): String {
        return when(this) {
            MemberType.NORMAL -> "普通"
            MemberType.MULTI_ENTRIES -> "次卡"
            MemberType.MONTHLY -> "月卡"
            MemberType.YEARLY -> "年卡"
        }
    }

    private fun toValidPeriod(validFrom: LocalDate, validUntil: LocalDate): String {
        return "${validFrom.toFormattedString()}-${validUntil.toFormattedString()}"
    }
}