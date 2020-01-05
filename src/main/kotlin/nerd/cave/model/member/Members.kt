package nerd.cave.model.member

import java.time.LocalDate

fun Member.isNormalMember(now: LocalDate): Boolean {
    return when(this.memberType) {
        MemberType.NORMAL -> true
        MemberType.ANNUAL -> (this.memberDetail as AnnualMember).expiredDate.isBefore(now)
        MemberType.MONTHLY -> (this.memberDetail as MonthlyMember).expiredDate.isBefore(now)
        MemberType.LIMITED_ENTRIES -> (this.memberDetail as LimitedEntriesMember).let { it.usedEntries >= it.totalEntries }
    }
}