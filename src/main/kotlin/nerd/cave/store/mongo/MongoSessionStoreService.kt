package nerd.cave.store.mongo

import nerd.cave.model.session.Session
import nerd.cave.store.SessionStoreService
import org.litote.kmongo.eq
import java.time.Clock
import java.time.ZonedDateTime
import java.util.*

class MongoSessionStoreService(private val clock: Clock, mongoStoreService: MongoStoreService): SessionStoreService {
    private val collection by lazy { mongoStoreService.getCollection<Session>() }

    override suspend fun newSession(userId: String): Session {
        val id = UUID.randomUUID().toString()
        val now = ZonedDateTime.now(clock)
        val session = Session(id, userId, now)
        collection.insertOne(session)
        return session
    }

    override suspend fun retrieveSession(id: String): Session? {
        return collection.findOne(Session::id eq id)
    }

}