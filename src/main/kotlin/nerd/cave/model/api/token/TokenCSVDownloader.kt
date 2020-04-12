package nerd.cave.model.api.token

import nerd.cave.model.api.member.MemberType
import nerd.cave.util.CSVWriter
import nerd.cave.util.toFormattedString
import nerd.cave.util.toSpreadSheetString

class TokenCSVDownloader(private val tokens: List<EnrichedToken>) {
    private val headers = linkedMapOf(
        "date" to "日期",
        "time" to "时间",
        "legalName" to "用户",
        "contactNumber" to "电话",
        "memberType" to "卡类",
        "branchName" to "下单地点"
    )

    fun toCSVString(): String {
        return CSVWriter(headers)
            .addRows( *tokens.map { it.toMap() }.toTypedArray() )
            .toCSVString()
    }

    private fun EnrichedToken.toMap(): Map<String, String?> {
        return mapOf(
            "date" to this.checkInDate.toFormattedString(),
            "time" to this.checkInTime.toLocalTime().toSpreadSheetString(),
            "legalName" to this.memberName,
            "contactNumber" to this.contactNumber,
            "memberType" to this.memberType.toDisplayName(),
            "branchName" to this.branchName
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
}