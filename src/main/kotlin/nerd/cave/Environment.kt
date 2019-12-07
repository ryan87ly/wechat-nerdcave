package nerd.cave

import nerd.cave.store.MongoConfig

enum class Environment(
    val mongoConfig: MongoConfig
) {
    LOCAL(MongoConfig.Local),
    PROD(MongoConfig.Prod);

    companion object {
        fun fromSystemEnv(): Environment {
            val env = System.getenv("APP_ENV") ?: "LOCAL"
            return valueOf(env.toUpperCase())
        }
    }
}