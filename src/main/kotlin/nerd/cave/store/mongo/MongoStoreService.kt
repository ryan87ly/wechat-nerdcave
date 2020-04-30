package nerd.cave.store.mongo

import nerd.cave.store.*
import nerd.cave.store.config.MongoConfig
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.util.KMongoConfiguration
import java.time.Clock
import javax.inject.Inject

class MongoStoreService @Inject constructor(private val config: MongoConfig, private val clock: Clock): StoreService {
    private lateinit var mongoClient: CoroutineClient
    lateinit var db: CoroutineDatabase
    override val memberStoreService by lazy { MongoMemberStoreService(clock, this) }
    override val memberEventStoreService by lazy { MongoMemberEventStoreService(this) }
    override val sessionStoreService by lazy { MongoSessionStoreService(clock, this) }
    override val productStoreService by lazy { MongoProductStoreService(clock, this) }
    override val branchStoreService by lazy { MongoBranchStoreService(this) }
    override val WXPaymentStoreService by lazy { MongoWXPaymentStoreService(this) }
    override val ticketStoreService by lazy { MongoTicketStoreService(this) }
    override val tokenStoreService by lazy { MongoTokenStoreService(this) }
    override val wxPaymentCallbackStoreService by lazy { MongoWXPaymentCallbackStoreService(this) }
    override val disclaimerStoreService by lazy { MongoDisclaimerStoreService(this) }
    override val offlineOrderStoreService by lazy { MongoOfflineOrderStoreService(this) }
    override val adminAccountStoreService by lazy { MongoAdminAccountStoreService(this) }
    override val adminSessionStoreService by lazy { MongoAdminSessionStoreService(clock, this) }
    override val publicHolidayStoreService: PublicHolidayStoreService by lazy { MongoPublicHolidayStoreService(this) }
    override val branchOpenStatusStoreService: BranchOpenStatusStoreService by lazy { MongoBranchOpenStatusStoreService(clock, this) }
    override val notificationStoreService: NotificationStoreService by lazy { MongoNotificationStoreService(this) }

    private val storeServices by lazy { listOf(memberStoreService, sessionStoreService, productStoreService) }

    override suspend fun start() {
        KMongoConfiguration.bsonMapper.registerModule(NerdCaveModule)
        mongoClient = KMongo.createClient(config.mongoClientSetting).coroutine
        db = mongoClient.getDatabase(config.dbName)
        db.listCollectionNames()

        storeServices.forEach { it.start() }
    }

    override suspend fun stop() {
        storeServices.reversed().forEach { it.stop() }
        mongoClient.close()
    }

    inline fun <reified T : Any> getCollection(collectionName: String): CoroutineCollection<T> {
        return db.getCollection(collectionName)
    }

    inline fun <reified T: Any> getCollection(): CoroutineCollection<T> {
        return db.getCollection(T::class.java.simpleName.toLowerCase())
    }

    inline fun <reified T: Any> getAdminCollection(): CoroutineCollection<T> {
        return db.getCollection("a_${T::class.java.simpleName.toLowerCase()}")
    }

}