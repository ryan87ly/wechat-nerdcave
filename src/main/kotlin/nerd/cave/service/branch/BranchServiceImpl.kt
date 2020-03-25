package nerd.cave.service.branch

import nerd.cave.model.api.branch.*
import nerd.cave.service.holiday.HolidayService
import nerd.cave.store.StoreService
import nerd.cave.web.exceptions.BadRequestException
import java.time.Clock
import java.time.LocalDate
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

    override suspend fun fetchBranchOpenStatus(branchId: String, date: LocalDate): BranchOpenStatus? {
        if (branchStoreService.fetchById(branchId) == null) throw BadRequestException("Branch not found by id [$branchId]")
        return branchOpenStatusStoreService.fetchBranchOpenStatus(branchId, date)
    }

    override suspend fun updateBranchOpenStatus(branchId: String, date: LocalDate, status: OpenStatus): Boolean {
        if (branchStoreService.fetchById(branchId) == null) throw BadRequestException("Branch not found by id [$branchId]")
        return branchOpenStatusStoreService.upsertBranchOpenStatus(branchId, date, status)
    }

    private suspend fun Branch.toBranchClientInfo(now: LocalDateTime): BranchClientInfo {
        val isOpen = isOpen(now)
        return BranchClientInfo(
            id,
            name,
            location,
            weekdayOpenHour,
            holidayOpenHour,
            contactNumbers,
            description,
            isOpen
        )
    }

    private suspend fun Branch.isOpen(now: LocalDateTime): Boolean {
        val currentDate = now.toLocalDate()
        val isHoliday = holidayService.isHoliday(currentDate)
        val presetOpenHourInfo = if (isHoliday) holidayOpenHour else weekdayOpenHour
        val localTime = now.toLocalTime()
        val branchOpenStatus = branchOpenStatusStoreService.fetchBranchOpenStatus(id, currentDate)
        return if (branchOpenStatus == null) {
            presetOpenHourInfo.isOpen(localTime)
        } else {
            return branchOpenStatus.status == OpenStatus.OPEN
        }
    }


}