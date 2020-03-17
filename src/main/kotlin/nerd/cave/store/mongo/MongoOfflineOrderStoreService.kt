package nerd.cave.store.mongo

import com.mongodb.client.model.IndexOptions
import nerd.cave.model.api.order.offline.ApprovalInfo
import nerd.cave.model.api.order.offline.OfflineOrder
import nerd.cave.model.api.order.offline.OfflineOrderStatus
import nerd.cave.store.OfflineOrderStoreService
import org.litote.kmongo.*
import java.time.ZonedDateTime

class MongoOfflineOrderStoreService(storeService: MongoStoreService): OfflineOrderStoreService {
    private val collection by lazy { storeService.getCollection<OfflineOrder>() }

    override suspend fun start() {
        collection.ensureIndex("id" eq 1, IndexOptions().unique(true))
    }

    override suspend fun insertOrder(order: OfflineOrder) {
        collection.insertOne(order)
    }

    override suspend fun fetchOrder(id: String): OfflineOrder? {
        val query = "id" eq id
        return collection.findOne(query)
    }

    override suspend fun approveOrder(id: String, approvalInfo: ApprovalInfo): Boolean {
        val query = "id" eq id
        val update = combine(
            setValue(OfflineOrder::status, OfflineOrderStatus.APPROVED),
            setValue(OfflineOrder::approvalInfo, approvalInfo)
        )
        return collection.updateOne(query, update).succeedUpdateOne()
    }

    override suspend fun fetchOrders(startTimeInclusive: ZonedDateTime?, endTimeExclusive: ZonedDateTime?): List<OfflineOrder> {
        return (if(startTimeInclusive != null && endTimeExclusive != null) {
            collection.find(
                and(
                    OfflineOrder::time gte startTimeInclusive,
                    OfflineOrder::time lt endTimeExclusive
                )
            )
        } else if (startTimeInclusive != null && endTimeExclusive == null) {
            collection.find(OfflineOrder::time gte startTimeInclusive)
        } else if (startTimeInclusive == null && endTimeExclusive != null) {
            collection.find(OfflineOrder::time lt endTimeExclusive)
        } else {
            collection.find()
        }).toList()
    }
}