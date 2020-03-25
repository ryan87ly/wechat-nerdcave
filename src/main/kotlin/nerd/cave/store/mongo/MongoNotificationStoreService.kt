package nerd.cave.store.mongo

import com.mongodb.client.model.IndexOptions
import nerd.cave.model.api.notification.Notification
import nerd.cave.store.NotificationStoreService
import org.litote.kmongo.descending

class MongoNotificationStoreService(mongoStoreService: MongoStoreService): NotificationStoreService {
    private val collection by lazy { mongoStoreService.getCollection<Notification> () }

    override suspend fun start() {
        collection.ensureIndex("time" eq 1, IndexOptions().unique(true))
    }

    override suspend fun insertNotification(notification: Notification) {
        collection.insertOne(notification)
    }

    override suspend fun latestNotification(): Notification? {
        return collection.find()
            .sort(
                descending(
                    Notification::time
                )
            )
            .first()
    }
}