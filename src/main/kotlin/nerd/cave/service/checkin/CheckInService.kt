package nerd.cave.service.checkin

import nerd.cave.model.token.Token
import java.time.LocalDate

interface CheckInService {
    suspend fun checkIn(memberId: String, branchId: String): Token?
    suspend fun fetchToken(memberId: String, checkInDate: LocalDate): Token?
    suspend fun checkInHistory(memberId: String, startDateInclusive: LocalDate, endDateExclusive: LocalDate?): List<String>
}