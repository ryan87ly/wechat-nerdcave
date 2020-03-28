package nerd.cave.net

import com.mongodb.ServerAddress

fun String.toServerAddress(): ServerAddress {
    val (host, port) = this.split(":")
    return ServerAddress(host, port.toInt())
}

fun String.toServerList(): List<ServerAddress> {
    return this.split(";")
        .map { it.toServerAddress() }
}