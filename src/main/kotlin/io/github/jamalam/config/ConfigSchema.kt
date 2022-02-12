package io.github.jamalam.config

import com.charleskorn.kaml.Yaml
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import java.nio.charset.Charset

@Serializable
data class BotConfig(
    val environment: String,
    val auth: AuthConfig,
    val production: ProductionConfig? = null,
    val development: DevelopmentConfig? = null
) {
    companion object {
        fun parse(filePath: String): BotConfig {
            val config = File(filePath).readText(Charset.defaultCharset())
            return Yaml.default.decodeFromString(serializer(), config)
        }
    }

    fun validate() {
        if (environment != "production" && environment != "development") {
            throw InvalidConfigException("parameter 'environment' must be equal to either 'production' or 'development'")
        }

        if (auth.productionBotToken == null && auth.developmentBotToken == null) {
            throw InvalidConfigException("one of 'production_bot_token' or 'testing_bot_token' must be specified")
        }

        if (environment == "production" && auth.productionBotToken == null) {
            throw InvalidConfigException("environment is set to 'production' but 'production_bot_token' is not set")
        }

        if (environment == "development" && auth.developmentBotToken == null) {
            throw InvalidConfigException("environment is set to 'development' but 'development_bot_token' is not set'")
        }

        if (environment == "production" && production == null) {
            throw InvalidConfigException("environment is set to 'production' but 'production' is not set")
        }

        if (environment == "development" && development == null) {
            throw InvalidConfigException("environment is set to 'development' but 'development' is not set")
        }
    }

    fun token(): String {
        return if (environment == "production") {
            auth.productionBotToken!!
        } else {
            auth.developmentBotToken!!
        }
    }

    fun production(): Boolean {
        return environment == "production"
    }
}

@Serializable
data class AuthConfig(
    @SerialName("production_bot_token")
    val productionBotToken: String? = null,
    @SerialName("development_bot_token")
    val developmentBotToken: String? = null,
    @SerialName("dbl_token")
    val dblToken: String? = null,
    @SerialName("sentry_url")
    val sentryUrl: String? = null,
    @SerialName("mongo_url")
    val mongoSrvUrl: String
)

@Serializable
data class ProductionConfig(
    @SerialName("admin_id")
    val adminId: Snowflake? = null,
    @SerialName("admin_server_id")
    val adminServerId: Snowflake? = null
)

@Serializable
data class DevelopmentConfig(
    @SerialName("admin_id")
    val adminId: Snowflake? = null,
    @SerialName("server_id")
    val serverId: Snowflake
)
