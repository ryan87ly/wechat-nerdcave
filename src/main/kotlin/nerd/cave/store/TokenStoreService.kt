package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.token.Token
import java.time.LocalDate

interface TokenStoreService: LifeCycle {
    suspend fun insertToken(token: Token)
    suspend fun fetchToken(memberId: String, checkInDate: LocalDate): Token?
    suspend fun history(memberId: String, startDateInclusive: LocalDate, endDateExclusive: LocalDate?): List<String>
}