package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.admin.Account
import nerd.cave.model.admin.AccountStatus
import nerd.cave.model.admin.Role

interface AdminAccountStoreService: LifeCycle {
    suspend fun newAccount(account: Account)
    suspend fun findById(id: String): Account?
    suspend fun findByUsernamePwd(username: String, password: String): Account?
    suspend fun usernameExists(username: String): Boolean
    suspend fun updatePassword(id: String, password: String): Boolean
    suspend fun updateRole(id: String, role: Role): Boolean
    suspend fun updateStatus(id: String, status: AccountStatus): Boolean
    suspend fun allAccounts(): List<Account>
}