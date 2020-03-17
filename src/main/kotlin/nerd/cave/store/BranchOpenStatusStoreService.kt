package nerd.cave.store

import nerd.cave.model.api.branch.BranchOpenStatus
import java.time.LocalDate

interface BranchOpenStatusStoreService {
    suspend fun updateBranchOpenStatus(branchId: String, date:LocalDate, isOpen: Boolean)
    suspend fun fetchBranchOpenStatus(branchId: String, date:LocalDate): BranchOpenStatus?
}