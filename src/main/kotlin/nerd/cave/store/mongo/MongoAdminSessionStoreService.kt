package nerd.cave.store.mongo

import com.mongodb.client.model.IndexOptions
import nerd.cave.model.admin.AdminSession
import nerd.cave.store.AdminSessionStoreService
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import org.litote.kmongo.setValue
import java.time.Clock
import java.time.ZonedDateTime
import java.util.*

class MongoAdminSessionStoreService(private val clock: Clock, mongoStoreService: MongoStoreService) : AdminSessionStoreService {
    private val collection by lazy { mongoStoreService.getAdminCollection<AdminSession>() }

    override suspend fun start() {
        collection.ensureIndex("id" eq 1, IndexOptions().unique(true))
    }

    override suspend fun newSession(adminId: String, validTimeSec: Long): AdminSession {
        val id = UUID.randomUUID().toString()
        val now = ZonedDateTime.now(clock)
        val expiryTime = now.plusSeconds(validTimeSec)
        val session = AdminSession(id, adminId, now, expiryTime, true)
        collection.insertOne(session)
        return session
    }

    override suspend fun retrieveSession(id: String): AdminSession? {
        val query = AdminSession::id eq id
        return collection.findOne(query)
    }

    override suspend fun renewSession(id: String, validTimeSec: Long): Boolean {
        val query = AdminSession::id eq id
        val updateExpiryTime = ZonedDateTime.now(clock).plusSeconds(validTimeSec)
        val update = setValue(AdminSession::expiryTime, updateExpiryTime)
        return collection.updateOne(query, update).succeedUpdateOne()
    }

    override suspend fun disableSessions(exclusiveId: String) {
        val query = AdminSession::id ne exclusiveId
        val update = setValue(AdminSession::active, false)
        collection.updateMany(query, update)
    }

}