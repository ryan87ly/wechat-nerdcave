package nerd.cave.store.mongo

import com.mongodb.client.model.IndexOptions
import nerd.cave.model.api.session.Session
import nerd.cave.store.SessionStoreService
import org.litote.kmongo.eq
import java.time.Clock
import java.time.ZonedDateTime
import java.util.*

class MongoSessionStoreService(private val clock: Clock, mongoStoreService: MongoStoreService): SessionStoreService {
    private val collection by lazy { mongoStoreService.getCollection<Session>() }

    override suspend fun start() {
        collection.ensureIndex("id" eq 1, IndexOptions().unique(true))
    }

    override suspend fun newSession(memberId: String): Session {
        val id = UUID.randomUUID().toString()
        val now = ZonedDateTime.now(clock)
        val session = Session(id, memberId, now)
        collection.insertOne(session)
        return session
    }

    override suspend fun retrieveSession(id: String): Session? {
        return collection.findOne(Session::id eq id)
    }

}