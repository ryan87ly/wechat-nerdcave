package nerd.cave.xml

import com.fasterxml.jackson.dataformat.xml.XmlMapper

internal val defaultMapper = XmlMapper()

fun Map<*, *>.toXmlString(): String {
    return defaultMapper.writeValueAsString(this)
}

@Suppress("UNCHECKED_CAST")
fun String.fromXmlToMap(): Map<String, String> {
    return defaultMapper.readValue(this, Map::class.java) as Map<String, String>
}