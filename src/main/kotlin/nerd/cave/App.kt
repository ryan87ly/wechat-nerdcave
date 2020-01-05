package nerd.cave

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("App Main")

fun main(args: Array<String>) = runBlocking<Unit> {
    val env = Environment.fromSystemEnv()
    val server = NerdCaveServer(env)
    try {
        server.start()
    } catch (t: Throwable) {
        logger.error("Error when starting MyServer $t")
    }
}
