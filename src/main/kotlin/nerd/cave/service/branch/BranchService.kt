package nerd.cave.service.branch

import nerd.cave.model.api.branch.Branch
import nerd.cave.model.api.branch.BranchClientInfo
import nerd.cave.model.api.branch.BranchOpenStatus
import nerd.cave.model.api.branch.OpenStatus
import java.time.LocalDate
import java.time.LocalDateTime

interface BranchService {
    suspend fun allBranchClientInfo(): List<BranchClientInfo>
    suspend fun findById(branchId: String): BranchClientInfo?
    suspend fun isBranchOpen(branch: Branch, time: LocalDateTime): Boolean
    suspend fun fetchBranchOpenStatus(branchId: String, date: LocalDate): BranchOpenStatus?
    suspend fun updateBranchOpenStatus(branchId: String, date: LocalDate, status: OpenStatus): Boolean
}