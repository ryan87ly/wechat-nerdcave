package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.api.notification.Notification

interface NotificationStoreService: LifeCycle {
    suspend fun insertNotification(notification: Notification)
    suspend fun latestNotification(): Notification?
}