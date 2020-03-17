package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.api.order.offline.ApprovalInfo
import nerd.cave.model.api.order.offline.OfflineOrder
import java.time.ZonedDateTime

interface OfflineOrderStoreService: LifeCycle {
    suspend fun insertOrder(order: OfflineOrder)
    suspend fun fetchOrder(id: String): OfflineOrder?
    suspend fun approveOrder(id: String, approvalInfo: ApprovalInfo): Boolean
    suspend fun fetchOrders(startTimeInclusive: ZonedDateTime?, endTimeExclusive: ZonedDateTime?): List<OfflineOrder>
}