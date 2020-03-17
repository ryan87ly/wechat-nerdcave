package nerd.cave.service.holiday

import java.time.LocalDate

interface HolidayService {
    suspend fun isHoliday(date: LocalDate): Boolean
}