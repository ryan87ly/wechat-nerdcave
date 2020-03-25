package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.api.token.Token
import java.time.LocalDate
import java.time.ZonedDateTime

interface TokenStoreService: LifeCycle {
    suspend fun insertToken(token: Token)
    suspend fun fetchToken(memberId: String, checkInDate: LocalDate): Token?
    suspend fun history(memberId: String, startDateInclusive: LocalDate, endDateExclusive: LocalDate?): List<String>
    suspend fun histories(startDateInclusive: LocalDate, endDateExclusive: LocalDate?): List<Token>
    suspend fun countByMemberId(startDateInclusive: LocalDate, endDateExclusive: LocalDate?): List<Pair<String, Long>>
    suspend fun countByBranch(branchId: String, startTimeInclusive: ZonedDateTime): Long
}