package nerd.cave.store.mongo

import com.mongodb.client.model.IndexOptions
import nerd.cave.model.admin.Account
import nerd.cave.store.AdminAccountStoreService
import org.litote.kmongo.and
import org.litote.kmongo.eq

class MongoAdminAccountStoreService(storeService: MongoStoreService): AdminAccountStoreService {
    private val collection by lazy { storeService.getAdminCollection<Account>() }

    override suspend fun start() {
        collection.ensureIndex("id" eq 1, IndexOptions().unique(true))
        collection.ensureIndex("username" eq 1, IndexOptions().unique(true))
    }

    override suspend fun newAccount(account: Account) {
        collection.insertOne(account)
    }

    override suspend fun findById(accountId: String): Account? {
        val query = "id" eq accountId
        return collection.findOne(query)
    }

    override suspend fun findByUsernamePwd(username: String, password: String): Account? {
        val query = and(
            Account::username eq username,
            Account::password eq password
        )
        return collection.findOne(query)
    }

}