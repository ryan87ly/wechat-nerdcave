package nerd.cave.service.checkin

import java.time.LocalDate

interface CheckInNumberGenerator {
    suspend fun nextNumber(date: LocalDate, branchId: String): String
}