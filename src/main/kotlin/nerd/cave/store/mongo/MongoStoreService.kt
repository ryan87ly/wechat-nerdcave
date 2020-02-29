package nerd.cave.store.mongo

import nerd.cave.store.*
import nerd.cave.store.config.MongoConfig
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.time.Clock

class MongoStoreService(private val config: MongoConfig, private val clock: Clock): StoreService {
    private lateinit var mongoClient: CoroutineClient
    lateinit var db: CoroutineDatabase
    override val memberStoreService: MemberStoreService by lazy { MongoMemberStoreService(clock, this) }
    override val memberEventStoreService: MemberEventStoreService by lazy { MongoMemberEventStoreService(this) }
    override val sessionStoreService: SessionStoreService by lazy { MongoSessionStoreService(clock, this) }
    override val productStoreService: ProductStoreService by lazy { MongoProductStoreService(clock, this) }
    override val branchStoreService: BranchStoreService by lazy { MongoBranchStoreService(this) }
    override val paymentStoreService: PaymentStoreService by lazy { MongoPaymentStoreService(this) }
    override val ticketStoreService: TicketStoreService by lazy { MongoTicketStoreService(this) }
    override val tokenStoreService: TokenStoreService by lazy { MongoTokenStoreService(this) }
    override val wxPaymentCallbackStoreService: WXPaymentCallbackStoreService by lazy { MongoWXPaymentCallbackStoreService(this) }
    override val disclaimerStoreService: DisclaimerStoreService by lazy { MongoDisclaimerStoreService(this) }

    private val storeServices by lazy { listOf(memberStoreService, sessionStoreService, productStoreService) }

    companion object {
        private val DB_NAME = "nerdcave"
    }

    override suspend fun start() {
        mongoClient = KMongo.createClient(config.mongoClientSetting).coroutine
        db = mongoClient.getDatabase(DB_NAME)
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

}