package nerd.cave.store.mongo

import com.mongodb.client.model.IndexOptions
import nerd.cave.model.api.branch.Branch
import nerd.cave.store.BranchStoreService
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class MongoBranchStoreService(mongoStoreService: MongoStoreService) : BranchStoreService {
    private val collection by lazy { mongoStoreService.getCollection<Branch>() }

    override suspend fun start() {
        collection.ensureIndex("id" eq 1, IndexOptions().unique(true))
    }

    override suspend fun fetchAll(): List<Branch> {
        return collection.find().toList()
    }

    override suspend fun fetchActiveBranches(): List<Branch> {
        val query = Branch::active eq true
        return collection.find(query).toList()
    }

    override suspend fun fetchById(id: String): Branch? {
        return collection.findOne(Branch::id eq id)
    }

    override suspend fun createBranch(branch: Branch) {
        collection.insertOne(branch)
    }

    override suspend fun deleteById(id: String): Boolean {
        val query = Branch::id eq id
        return collection.deleteOne(query).deletedCount == 1L
    }

    override suspend fun deactivate(id: String): Boolean {
        val query = Branch::id eq id
        val update = setValue(Branch::active, false)
        return collection.updateOne(query, update).succeedUpdateOne()
    }


}