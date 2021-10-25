package io.github.jamalam360.database

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.mongodb.client.MongoDatabase
import dev.kord.common.entity.Snowflake
import io.github.jamalam360.DATABASE
import io.github.jamalam360.database.migration.migrate
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.updateOne

/**
 *  Represents the collections of config documents in the DB.
 *  @author  Jamalam360
 */
@Suppress("RemoveExplicitTypeArguments")
class ConfigCollection(db: MongoDatabase) : DatabaseCollection<ServerConfig>(db.getCollection<ServerConfig>()) {
    private val configCache: HashMap<Long, ServerConfig> = HashMap<Long, ServerConfig>()

    /**
     *  Gets a config from the DB, using the cached value if possible. If a cached value is not available, it will be cached for next time.
     *  If a config has not yet been created, a default one will be instantiated and saved to the DB and cache.
     *
     *  @param id the id of the server
     *  @return the fetched config
     */
    fun getConfig(id: Snowflake): ServerConfig {
        val conf: ServerConfig

        if (!hasConfig(id)) {
            conf = createDefaultConfig(id)
            configCache[id.value] = conf
        } else {
            if (!configCache.containsKey(id.value)) {
                conf = collection.findOne(ServerConfig::id eq id.value)!!
                configCache[id.value] = conf
            } else {
                conf = configCache[id.value]!!
            }
        }

        return conf
    }

    /**
     *  Updates a config inside the DB and cache, any unmodified values will stay the same.
     *
     *  @param id the id of the server
     *  @param updated the updated config object
     */
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
                moderatorRole = 0,

                mutableListOf<Long>()
            )
        )

        collection.insertOne(config)
        return config
    }

    /**
     *  Checks whether the DB holds a config for a specific server, and if the config is incomplete/incompatible
     *  with the current schema, migrates it and returns the migrated config.
     *  @param id the id of the server
     *  @return whether the DB has the config
     */
    private fun hasConfig(id: Snowflake): Boolean {
        return try {
            collection.findOne(ServerConfig::id eq id.value) != null
        } catch (e: MissingKotlinParameterException) {
            migrate(DATABASE.db)
            hasConfig(id)
        }
    }

    /**
     * Checks whether a module is enabled on a server.
     * @param id the id of the server
     * @param module the module to check
     * @return whether the module is enabled
     */
    fun isModuleEnabled(id: Snowflake, module: Modules): Boolean {
        val config = getConfig(id)

        return when (module) {
            Modules.Quotes -> config.quotesConfig.enabled
            Modules.Moderation -> config.moderationConfig.enabled
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
    var moderatorRole: Long,

    var threadAutoJoinRoles: MutableList<Long>
)

enum class Modules(val readableName: String) {
    Quotes("quotes"),
    Moderation("moderation"),
    Logging("logging")
}
