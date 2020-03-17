package nerd.cave.service.checkin

import nerd.cave.model.api.member.Member
import nerd.cave.model.api.token.Token
import java.time.LocalDate

interface CheckInService {
    suspend fun checkIn(member: Member, branchId: String): Token
    suspend fun fetchToken(memberId: String, checkInDate: LocalDate): Token?
    suspend fun checkInHistory(memberId: String, startDateInclusive: LocalDate, endDateExclusive: LocalDate?): List<String>
    suspend fun countByMemberId(startDateInclusive: LocalDate, endDateExclusive: LocalDate?): List<Pair<String, Long>>
}