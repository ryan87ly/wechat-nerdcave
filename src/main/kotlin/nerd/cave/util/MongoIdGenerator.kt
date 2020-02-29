package nerd.cave.util

import org.bson.types.ObjectId

class MongoIdGenerator: IdGenerator {

    override fun nextId(): String {
        return ObjectId.get().toString()
    }
}