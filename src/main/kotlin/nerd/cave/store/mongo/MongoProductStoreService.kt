package nerd.cave.store.mongo

import com.mongodb.client.model.IndexOptions
import nerd.cave.model.product.Product
import nerd.cave.store.ProductStoreService
import java.time.Clock

class MongoProductStoreService(private val clock: Clock, mongoStoreService: MongoStoreService): ProductStoreService {
    private val collection = mongoStoreService.getCollection<Product>()

    override suspend fun start() {
        collection.ensureIndex("id" eq 1, IndexOptions().unique(true))
    }

    override suspend fun fetchAll(): List<Product> {
        return collection.find().toList()
    }

    override suspend fun fetchById(id: String): Product? {
        return collection.findOne("id" eq id)
    }

    override suspend fun createProduct(product: Product) {
        collection.insertOne(product)
    }

    override suspend fun deleteById(id: String): Boolean {
        val query = "id" eq id
        return collection.deleteOne(query).deletedCount == 1L
    }


}