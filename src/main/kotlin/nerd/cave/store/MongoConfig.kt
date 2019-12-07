package nerd.cave.store

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress

interface MongoConfig {
    val mongoClientSetting: MongoClientSettings

    object Local : MongoConfig {
        override val mongoClientSetting: MongoClientSettings
            get() = MongoClientSettings.builder().apply {
                applyToClusterSettings {
                    it.hosts(listOf(
                        ServerAddress("localhost", 27017)
                    ))
                }
            }.build()
    }

    object Prod: MongoConfig {
        override val mongoClientSetting: MongoClientSettings
            get() = MongoClientSettings.builder().apply {
                applyToClusterSettings {
                    it.hosts(listOf(
                        ServerAddress("193.112.62.230", 27017)
                    ))
                    credential(MongoCredential.createCredential("nerdcave", "admin", System.getenv("DB_PASSWORD").toCharArray()))
                }
            }.build()
    }
}

