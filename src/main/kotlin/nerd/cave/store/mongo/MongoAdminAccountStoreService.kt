package nerd.cave.store.mongo

import com.mongodb.client.model.IndexOptions
import nerd.cave.model.admin.Account
import nerd.cave.model.admin.AccountStatus
import nerd.cave.model.admin.Role
import nerd.cave.store.AdminAccountStoreService
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class MongoAdminAccountStoreService(storeService: MongoStoreService): AdminAccountStoreService {
    private val collection by lazy { storeService.getAdminCollection<Account>() }

    override suspend fun start() {
        collection.ensureIndex("id" eq 1, IndexOptions().unique(true))
        collection.ensureIndex("username" eq 1, IndexOptions().unique(true))
    }

    override suspend fun newAccount(account: Account) {
        collection.insertOne(account)
    }

    override suspend fun findById(id: String): Account? {
        val query = Account::id eq id
        return collection.findOne(query)
    }

    override suspend fun findByUsernamePwd(username: String, password: String): Account? {
        val query = and(
            Account::username eq username,
            Account::password eq password
        )
        return collection.findOne(query)
    }

    override suspend fun usernameExists(username: String): Boolean {
        val query = Account::username eq username
        return collection.countDocuments(query) > 0L
    }

    override suspend fun updatePassword(id: String, password: String): Boolean {
        val query = Account::id eq id
        val update = setValue(Account::password, password)
        return collection.updateOne(query, update).succeedUpdateOne()
    }

    override suspend fun updateRole(id: String, role: Role): Boolean {
        val query = Account::id eq id
        val update = setValue(Account::role, role)
        return collection.updateOne(query, update).succeedUpdateOne()
    }

    override suspend fun updateStatus(id: String, status: AccountStatus): Boolean {
        val query = Account::id eq id
        val update = setValue(Account::status, status)
        return collection.updateOne(query, update).succeedUpdateOne()
    }

    override suspend fun allAccounts(): List<Account> {
        return collection.find().toList()
    }

}