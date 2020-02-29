package nerd.cave.store.mongo

import com.mongodb.client.model.IndexOptions
import nerd.cave.model.branch.Branch
import nerd.cave.store.BranchStoreService
import java.time.Clock

class MongoBranchStoreService(mongoStoreService: MongoStoreService): BranchStoreService {
    private val collection by lazy {  mongoStoreService.getCollection<Branch>() }

    override suspend fun start() {
        collection.ensureIndex("id" eq 1, IndexOptions().unique(true))
    }

    override suspend fun fetchAll(): List<Branch> {
        return collection.find().toList()
    }

    override suspend fun fetchById(id: String): Branch? {
        return collection.findOne("id" eq id)
    }

    override suspend fun createBranch(branch: Branch) {
        collection.insertOne(branch)
    }

    override suspend fun deleteById(id: String): Boolean {
        val query = "id" eq id
        return collection.deleteOne(query).deletedCount == 1L
    }


}