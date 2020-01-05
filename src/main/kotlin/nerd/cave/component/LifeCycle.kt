package nerd.cave.component

interface LifeCycle {
    suspend fun start() = run { }
    suspend fun stop() = run { }
}