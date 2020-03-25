package nerd.cave.store.mongo

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.UpdateOptions
import nerd.cave.model.api.branch.BranchOpenStatus
import nerd.cave.model.api.branch.OpenStatus
import nerd.cave.store.BranchOpenStatusStoreService
import nerd.cave.util.toFormattedString
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import java.time.Clock
import java.time.LocalDate
import java.time.ZonedDateTime

class MongoBranchOpenStatusStoreService(private val clock: Clock, storeService: MongoStoreService): BranchOpenStatusStoreService {
    private val collection by lazy { storeService.getAdminCollection<BranchOpenStatus>() }

    override suspend fun start() {
        collection.ensureIndex(
            and("branchId" eq 1, "date" eq 1), IndexOptions().unique(true)
        )
    }

    override suspend fun upsertBranchOpenStatus(branchId: String, date: LocalDate, status: OpenStatus): Boolean {
        val query = and(
            BranchOpenStatus::branchId eq branchId,
            "date" eq date.toFormattedString()
        )
        val now = ZonedDateTime.now(clock)
        val update = combine(
            setValue(BranchOpenStatus::status, status),
            setValue(BranchOpenStatus::updatedTime, now)
        )
        return collection.updateOne(query, update, UpdateOptions().upsert(true)).succeedUpsertOne()
    }

    override suspend fun fetchBranchOpenStatus(branchId: String, date: LocalDate): BranchOpenStatus? {
        val query = and(
            BranchOpenStatus::branchId eq branchId,
            "date" eq date.toFormattedString()
        )
        return collection.findOne(query)
    }


}