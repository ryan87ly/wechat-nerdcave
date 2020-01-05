package nerd.cave

import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking {
    val env = Environment.LOCAL
    val server = NerdCaveServer(env)
    try {
        server.start()
    } catch (t: Throwable) {
        logger.error("Error when starting MyServer $t")
    }
}
