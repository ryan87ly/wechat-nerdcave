package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.member.MemberEvent

interface MemberEventStoreService: LifeCycle {
    suspend fun insertEvent(memberEvent: MemberEvent)
    suspend fun queryEvents(memberId: String): List<MemberEvent>
}