package nerd.cave.core

import com.google.inject.AbstractModule
import com.google.inject.Provides
import io.vertx.core.Vertx
import nerd.cave.Environment
import nerd.cave.util.TIME_ZONE
import nerd.cave.web.client.WebClient
import java.time.Clock
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Singleton

class BasicModule(private val env: Environment): AbstractModule() {

    override fun configure() {
        bind(WebClient::class.java).`in`(Singleton::class.java)
    }

    @Provides
    @Singleton
    fun provideVertx(): Vertx {
        return Vertx.vertx()
    }

    @Provides
    @Singleton
    fun provideClock(): Clock {
        return Clock.system(TIME_ZONE)
    }

    @Provides
    @Singleton
    fun provideExecutor(): Executor {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    }

    @Provides
    @Singleton
    fun provideEnvironment(): Environment {
        return env
    }
}