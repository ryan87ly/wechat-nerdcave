package nerd.cave

import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking<Unit> {
    val server = MyServer()
    try {
        server.start()
    } catch (t: Throwable) {
        println("Error when starting MyServer $t")
    }


}
