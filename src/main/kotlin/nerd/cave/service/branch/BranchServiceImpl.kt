package nerd.cave.service.branch

import nerd.cave.model.api.branch.Branch
import nerd.cave.model.api.branch.BranchClientInfo
import nerd.cave.model.api.branch.isOpen
import nerd.cave.service.holiday.HolidayService
import nerd.cave.store.StoreService
import java.time.Clock
import java.time.LocalDateTime

class BranchServiceImpl(private val clock:Clock, storeService: StoreService, private val holidayService: HolidayService): BranchService {
    private val branchStoreService by lazy { storeService.branchStoreService }
    private val branchOpenStatusStoreService by lazy { storeService.branchOpenStatusStoreService }

    override suspend fun allBranchClientInfo(): List<BranchClientInfo> {
        val now = LocalDateTime.now(clock)
        return branchStoreService.fetchActiveBranches()
            .map { it.toBranchClientInfo(now) }
    }

    override suspend fun findById(branchId: String): BranchClientInfo? {
        return branchStoreService.fetchById(branchId)?.toBranchClientInfo(LocalDateTime.now(clock))
    }

    override suspend fun isBranchOpen(branch: Branch, time: LocalDateTime): Boolean {
        return branch.isOpen(time)
    }

    private suspend fun Branch.toBranchClientInfo(now: LocalDateTime): BranchClientInfo {
        val isOpen = isOpen(now)
        return BranchClientInfo(
            id,
            name,
            location,
            weekdayOpenHour,
            holidayOpenHourInfo,
            contactNumbers,
            description,
            isOpen
        )
    }

    private suspend fun Branch.isOpen(now: LocalDateTime): Boolean {
        val currentDate = now.toLocalDate()
        val isHoliday = holidayService.isHoliday(currentDate)
        val presetOpenHourInfo = if (isHoliday) holidayOpenHourInfo else weekdayOpenHour
        val localTime = now.toLocalTime()
        val branchOpenStatus = branchOpenStatusStoreService.fetchBranchOpenStatus(id, currentDate)
        return if (branchOpenStatus == null) {
            presetOpenHourInfo.isOpen(localTime)
        } else {
            val updatedLocalTime = branchOpenStatus.updatedTime.withZoneSameInstant(clock.zone).toLocalTime()
            if (presetOpenHourInfo.isOpen(localTime)) {
                if(presetOpenHourInfo.isOpen(updatedLocalTime)) branchOpenStatus.isOpen else true
            } else if (localTime.isBefore(presetOpenHourInfo.openTime)) {
                if(updatedLocalTime.isBefore(presetOpenHourInfo.openTime)) branchOpenStatus.isOpen else false
            } else {
                if(!updatedLocalTime.isBefore(presetOpenHourInfo.closeTime)) branchOpenStatus.isOpen else false
            }
        }
    }


}