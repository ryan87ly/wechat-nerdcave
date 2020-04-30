package nerd.cave.service.holiday

import nerd.cave.store.StoreService
import nerd.cave.util.isWeekend
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class HolidayServiceImpl @Inject constructor(private val clock: Clock, storeService: StoreService): HolidayService {
    private val publicHolidayStoreService by lazy { storeService.publicHolidayStoreService }

    override suspend fun isHoliday(date: LocalDate): Boolean {
        return date.isWeekend() || publicHolidayStoreService.hasRecord(date)
    }
}