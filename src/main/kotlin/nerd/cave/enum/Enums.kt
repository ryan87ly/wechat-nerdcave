package nerd.cave.enum

import java.lang.IllegalArgumentException

inline fun <reified T: Enum<T>> fromString(str: String): T? {
    return try {
        enumValueOf<T>(str)
    } catch (e: IllegalArgumentException) {
        null
    }
}