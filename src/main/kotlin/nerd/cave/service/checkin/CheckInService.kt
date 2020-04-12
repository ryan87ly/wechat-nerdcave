package nerd.cave.service.checkin

import nerd.cave.model.api.member.Member
import nerd.cave.model.api.token.EnrichedToken
import nerd.cave.model.api.token.Token
import java.time.LocalDate
import java.time.ZonedDateTime

interface CheckInService {
    suspend fun checkIn(member: Member, branchId: String): Token
    suspend fun fetchToken(memberId: String, checkInDate: LocalDate): Token?
    suspend fun checkInHistory(memberId: String, startDateInclusive: LocalDate, endDateExclusive: LocalDate?): List<String>
    suspend fun allCheckInHistory(): List<EnrichedToken>
    suspend fun membersCheckInHistory(startDateInclusive: LocalDate, endDateExclusive: LocalDate?): List<EnrichedToken>
    suspend fun countByMemberId(startDateInclusive: LocalDate, endDateExclusive: LocalDate?): List<Pair<String, Long>>
    suspend fun fetchRecentCheckIns(branchId: String): Long

}