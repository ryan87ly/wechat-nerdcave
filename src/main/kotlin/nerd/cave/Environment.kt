package nerd.cave

enum class Environment{
    LOCAL,
    UAT,
    PROD;

    companion object {
        fun fromSystemEnv(): Environment {
            val env = System.getenv("APP_ENV") ?: "LOCAL"
            return valueOf(env.toUpperCase())
        }
    }
}