package nerd.cave.store.mongo

import com.mongodb.client.model.Filters
import org.bson.conversions.Bson

infix fun String.eq(other: Any): Bson {
    return Filters.eq(this, other)
}

