package nerd.cave.store.mongo

import com.mongodb.client.result.UpdateResult

fun UpdateResult.succeedUpdateOne(): Boolean {
    return modifiedCount == 1L
}