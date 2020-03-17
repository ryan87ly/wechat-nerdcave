package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.admin.AdminSession

interface AdminSessionStoreService: LifeCycle {
    suspend fun newSession(adminId: String, validTimeSec: Long): AdminSession
    suspend fun retrieveSession(id: String): AdminSession?
    suspend fun renewSession(id: String, validTimeSec: Long): Boolean
    suspend fun disableSessions(exclusiveId:String)
}