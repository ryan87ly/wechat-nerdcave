package nerd.cave.service.branch

import nerd.cave.model.api.branch.Branch
import nerd.cave.model.api.branch.BranchClientInfo
import java.time.LocalDateTime

interface BranchService {
    suspend fun allBranchClientInfo(): List<BranchClientInfo>
    suspend fun findById(branchId: String): BranchClientInfo?
    suspend fun isBranchOpen(branch: Branch, time: LocalDateTime): Boolean
}