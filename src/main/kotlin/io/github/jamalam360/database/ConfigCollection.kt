package io.github.jamalam360.database

import com.mongodb.client.MongoDatabase
import dev.kord.common.entity.Snowflake
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

/**
 * @author  Jamalam360
 */
@Suppress("RemoveExplicitTypeArguments")
class ConfigCollection(db: MongoDatabase) : DatabaseCollection<ServerConfig>(db.getCollection<ServerConfig>()) {
    fun getConfig(id: Snowflake): ServerConfig {
        return if (!hasConfig(id)) {
            val config = ServerConfig(
                id
            )
            collection.insertOne(config)
            config
        } else {
            collection.findOne(ServerConfig::id eq id)!!
        }
    }

    fun hasConfig(id: Snowflake): Boolean = collection.findOne(ServerConfig::id eq id) != null
}

data class ServerConfig(val id: Snowflake)
