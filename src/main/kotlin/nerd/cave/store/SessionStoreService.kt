package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.api.session.Session

interface SessionStoreService: LifeCycle {
    suspend fun newSession(memberId: String): Session
    suspend fun retrieveSession(id: String): Session?
}