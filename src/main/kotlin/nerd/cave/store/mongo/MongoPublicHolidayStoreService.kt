package nerd.cave.store.mongo

import com.mongodb.client.model.IndexOptions
import nerd.cave.model.admin.PublicHoliday
import nerd.cave.store.PublicHolidayStoreService
import nerd.cave.util.toFormattedString
import java.time.LocalDate

class MongoPublicHolidayStoreService(storeService: MongoStoreService): PublicHolidayStoreService {
    private val collection by lazy { storeService.getAdminCollection<PublicHoliday> () }

    override suspend fun start() {
        collection.ensureIndex("date" eq 1, IndexOptions().unique(true))
    }

    override suspend fun hasRecord(date: LocalDate): Boolean {
        return collection.countDocuments("date" eq date.toFormattedString()) > 0L
    }

    override suspend fun getHolidays(year: Int): List<PublicHoliday> {
        val pattern = "^$year\\d+"
        val query = "date" regex pattern
        return collection.find(query).toList()
    }

    override suspend fun addHoliday(date: LocalDate) {
        collection.insertOne(PublicHoliday(date))
    }

    override suspend fun removeHoliday(date: LocalDate): Boolean {
        return collection.deleteOne("date" eq date.toFormattedString()).deletedCount == 1L
    }
}