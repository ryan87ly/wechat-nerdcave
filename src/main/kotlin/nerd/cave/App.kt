package nerd.cave

import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking<Unit> {
    val env = Environment.fromSystemEnv()
    val server = NerdCaveServer(env)
    try {
        server.start()
    } catch (t: Throwable) {
        println("Error when starting MyServer $t")
    }
}
