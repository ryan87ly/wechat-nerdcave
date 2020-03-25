package nerd.cave.store.mongo

import com.mongodb.client.model.Filters
import org.bson.conversions.Bson

infix fun String.eq(other: Any): Bson {
    return Filters.eq(this, other)
}

infix fun String.lt(other: Any): Bson {
    return Filters.lt(this, other)
}

infix fun String.gte(other: Any): Bson {
    return Filters.gte(this, other)
}

infix fun String.regex(pattern: String): Bson {
    return Filters.regex(this, pattern)
}

infix fun Bson.and(other: Bson): Bson {
    return Filters.and(
        this,
        other
    )
}


