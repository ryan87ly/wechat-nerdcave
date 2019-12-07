package nerd.cave.store

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoDatabase
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

class MongoStoreService(val config: MongoConfig): StoreService {
    private lateinit var mongoClient: MongoClient
    private lateinit var db: MongoDatabase

    companion object {
        private val DB_NAME = "NerdCave";
    }

    suspend fun start() {
        mongoClient = KMongo.createClient(config.mongoClientSetting).coroutine.client
        db = mongoClient.getDatabase(DB_NAME)
        db.listCollectionNames()
    }
}