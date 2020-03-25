package nerd.cave.store.mongo

import com.mongodb.client.result.UpdateResult

fun UpdateResult.succeedUpdateOne(): Boolean {
    return matchedCount == 1L
}

fun UpdateResult.succeedUpsertOne(): Boolean {
    return matchedCount == 1L || (upsertedId != null)
}