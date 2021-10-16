package io.github.jamalam360.database

import com.mongodb.client.MongoDatabase
import dev.kord.common.entity.Snowflake
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.updateOne

/**
 * @author  Jamalam360
 */
@Suppress("RemoveExplicitTypeArguments")
class ConfigCollection(db: MongoDatabase) : DatabaseCollection<ServerConfig>(db.getCollection<ServerConfig>()) {
    private val configCache: HashMap<Long, ServerConfig> = HashMap<Long, ServerConfig>()

    fun getConfig(id: Snowflake): ServerConfig {
        val conf: ServerConfig

        if (!hasConfig(id)) { // Create a default config and save it to the cache
            conf = createDefaultConfig(id)
            configCache[id.value] = conf
        } else {
            if (!configCache.containsKey(id.value)) { // Get the config  from the DB and save it to the cache
                conf = collection.findOne(ServerConfig::id eq id.value)!!
                configCache[id.value] = conf
            } else { // Get the config from the cache
                conf = configCache[id.value]!!
            }
        }

        return conf
    }

    fun updateConfig(id: Snowflake, updated: ServerConfig) {
        collection.updateOne(ServerConfig::id eq id.value, updated)
        configCache[id.value] = updated
    }

    private fun createDefaultConfig(id: Snowflake): ServerConfig {
        val config = ServerConfig(
            id.value,

            ServerQuotesConfig(
                true,
                null,
                true
            ),

            ServerLoggingConfig(
                true,
                null
            ),

            ServerModerationConfig(
                enabled = true,
                logActions = true,
                moderatorRole = 0
            )
        )

        collection.insertOne(config)
        return config
    }

    private fun hasConfig(id: Snowflake): Boolean = collection.findOne(ServerConfig::id eq id.value) != null

    fun isModuleEnabled(id: Snowflake, module: Modules): Boolean {
        val config = getConfig(id)

        return when (module) {
            Modules.Quotes -> config.quotesConfig.enabled
            Modules.Moderation -> true
            Modules.Logging -> config.loggingConfig.enabled
        }
    }
}

data class ServerConfig(
    var id: Long,

    var quotesConfig: ServerQuotesConfig,
    var loggingConfig: ServerLoggingConfig,
    var moderationConfig: ServerModerationConfig
)

data class ServerQuotesConfig(
    var enabled: Boolean,
    var channel: Long?,
    var log: Boolean
)

data class ServerLoggingConfig(
    var enabled: Boolean,
    var channel: Long?
)

data class ServerModerationConfig(
    var enabled: Boolean,
    var logActions: Boolean,
    var moderatorRole: Long
)

enum class Modules(val readableName: String) {
    Quotes("quotes"),
    Moderation("moderation"),
    Logging("logging")
}
