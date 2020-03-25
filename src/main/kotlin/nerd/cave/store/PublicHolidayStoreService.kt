package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.admin.PublicHoliday
import java.time.LocalDate

interface PublicHolidayStoreService: LifeCycle {
    suspend fun hasRecord(date: LocalDate): Boolean
    suspend fun getHolidays(year: Int): List<PublicHoliday>
    suspend fun addHoliday(date: LocalDate)
    suspend fun removeHoliday(date: LocalDate): Boolean
}