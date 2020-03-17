package nerd.cave.store

import nerd.cave.component.LifeCycle
import java.time.LocalDate

interface PublicHolidayStoreService: LifeCycle {
    suspend fun hasRecord(date: LocalDate): Boolean
    suspend fun addHoliday(date: LocalDate)
    suspend fun removeHoliday(date: LocalDate): Boolean
}