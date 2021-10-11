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
    fun getConfig(id: Snowflake): ServerConfig {
        return if (!hasConfig(id)) {
            createDefaultConfig(id)
        } else {
            collection.findOne(ServerConfig::id eq id.value)!!
        }
    }

    fun updateConfig(id: Snowflake, updated: ServerConfig) {
        collection.updateOne(ServerConfig::id eq id.value, updated)
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
            )
        )

        collection.insertOne(config)
        return config
    }

    private fun hasConfig(id: Snowflake): Boolean = collection.findOne(ServerConfig::id eq id.value) != null
}

data class ServerConfig(
    var id: Long,

    var quotesConfig: ServerQuotesConfig,
    var loggingConfig: ServerLoggingConfig
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
