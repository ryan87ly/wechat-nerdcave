package nerd.cave.service.checkin

import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates.inc
import nerd.cave.model.token.toTokenDateFormat
import nerd.cave.store.mongo.MongoStoreService
import nerd.cave.store.mongo.eq
import org.bson.Document
import java.time.LocalDate

class CheckInNumberGeneratorMongoImpl(mongoStoreService: MongoStoreService): CheckInNumberGenerator {
    private val collection by lazy { mongoStoreService.getCollection<Document>("CheckInNumber") }
    private companion object {
        val MOD = 100
    }
    override suspend fun nextNumber(date: LocalDate, branchId: String): String {
        val query = and(
            "branchId" eq branchId,
            "date" eq date.toTokenDateFormat()
        )
        val update = inc(
            "count", 1
        )
        val count = collection.findOneAndUpdate(
            query,
            update,
            FindOneAndUpdateOptions().apply {
                upsert(true)
                returnDocument(ReturnDocument.AFTER)
        })!!.getInteger("count")
        return "${prependZeroIfNeeded(date.dayOfMonth)}${prependZeroIfNeeded(count % MOD)}"
    }

    private fun prependZeroIfNeeded(dayOfMonth: Int): String {
        return if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth.toString()
    }
}