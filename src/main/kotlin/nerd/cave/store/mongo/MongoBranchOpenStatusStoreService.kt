package nerd.cave.store.mongo

import nerd.cave.model.api.branch.BranchOpenStatus
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

    override suspend fun updateBranchOpenStatus(branchId: String, date: LocalDate, isOpen: Boolean) {
        val query = and(
            BranchOpenStatus::branchId eq branchId,
            "date" eq date.toFormattedString()
        )
        val now = ZonedDateTime.now(clock)
        val update = combine(
            setValue(BranchOpenStatus::isOpen, isOpen),
            setValue(BranchOpenStatus::updatedTime, now)
        )
        collection.updateOne(query, update)
    }

    override suspend fun fetchBranchOpenStatus(branchId: String, date: LocalDate): BranchOpenStatus? {
        val query = and(
            BranchOpenStatus::branchId eq branchId,
            "date" eq date.toFormattedString()
        )
        return collection.findOne(query)
    }


}