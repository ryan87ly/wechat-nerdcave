package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.api.branch.BranchOpenStatus
import nerd.cave.model.api.branch.OpenStatus
import java.time.LocalDate

interface BranchOpenStatusStoreService: LifeCycle {
    suspend fun upsertBranchOpenStatus(branchId: String, date:LocalDate, status: OpenStatus): Boolean
    suspend fun fetchBranchOpenStatus(branchId: String, date:LocalDate): BranchOpenStatus?
}