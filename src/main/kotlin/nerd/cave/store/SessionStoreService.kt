package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.session.Session

interface SessionStoreService: LifeCycle {
    suspend fun newSession(userId: String): Session
    suspend fun retrieveSession(id: String): Session?
}