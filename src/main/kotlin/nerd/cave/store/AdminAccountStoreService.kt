package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.admin.Account

interface AdminAccountStoreService: LifeCycle {
    suspend fun newAccount(account: Account)
    suspend fun findById(accountId: String): Account?
    suspend fun findByUsernamePwd(username: String, password: String): Account?
}