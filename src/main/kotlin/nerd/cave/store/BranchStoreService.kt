package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.branch.Branch

interface BranchStoreService: LifeCycle {
    suspend fun fetchAll(): List<Branch>
    suspend fun fetchById(id: String): Branch?
    suspend fun createBranch(branch: Branch)
    suspend fun deleteById(id: String): Boolean
}