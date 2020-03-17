package nerd.cave.store.config

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import nerd.cave.Environment

interface MongoConfig {
    val mongoClientSetting: MongoClientSettings
    val dbName:String get() = "nerdcave"

    companion object {
        fun forEnv(env: Environment): MongoConfig {
            return when(env) {
                Environment.LOCAL -> Local
                Environment.UAT -> Prod
                Environment.PROD -> Prod
            }
        }
    }

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
                        ServerAddress("172.16.0.14", 27017)
                    ))
                    credential(MongoCredential.createCredential("nerdcave", "admin", System.getenv("DB_PASSWORD").toCharArray()))
                }
            }.build()
    }


}

