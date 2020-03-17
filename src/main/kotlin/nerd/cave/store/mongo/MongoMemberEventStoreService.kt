package nerd.cave.store.mongo

import nerd.cave.model.api.member.MemberEvent
import nerd.cave.store.MemberEventStoreService

class MongoMemberEventStoreService(storeService: MongoStoreService): MemberEventStoreService {

    private val collection by lazy { storeService.getCollection<MemberEvent>() }

    override suspend fun insertEvent(memberEvent: MemberEvent) {
        collection.insertOne(memberEvent)
    }

    override suspend fun queryEvents(memberId: String): List<MemberEvent> {
        val query = "memberId" eq memberId
        return collection.find(query).toList()
    }
}