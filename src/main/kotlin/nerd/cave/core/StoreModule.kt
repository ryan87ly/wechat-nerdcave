package nerd.cave.core

import com.google.inject.AbstractModule
import com.google.inject.Provides
import nerd.cave.Environment
import nerd.cave.store.StoreService
import nerd.cave.store.config.MongoConfig
import nerd.cave.store.mongo.MongoStoreService
import javax.inject.Singleton

class StoreModule(private val env: Environment): AbstractModule()  {

    override fun configure() {
        bind(MongoStoreService::class.java).`in`(Singleton::class.java)
        bind(StoreService::class.java).to(MongoStoreService::class.java)
    }

    @Provides
    @Singleton
    fun provideMongoConfig(): MongoConfig {
        return MongoConfig.forEnv(env)
    }
}