/*
 * Copyright (C) 2022 Jamalam
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.jamalam.database

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.mongodb.client.MongoDatabase
import dev.kord.common.entity.Snowflake
import io.github.jamalam.Modules
import io.github.jamalam.database.entity.ServerConfig
import io.github.jamalam.database.migration.migrate
import io.github.jamalam.util.database
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
            conf = ServerConfig::class.getDefault(id)
            collection.insertOne(conf)
            configCache[id.value.toLong()] = conf
        } else {
            if (!configCache.containsKey(id.value.toLong())) {
                conf = collection.findOne(ServerConfig::id eq id.value.toLong())!!
                configCache[id.value.toLong()] = conf
            } else {
                conf = configCache[id.value.toLong()]!!
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
        collection.updateOne(ServerConfig::id eq id.value.toLong(), updated)
        configCache[id.value.toLong()] = updated
    }

    fun deleteConfig(id: Snowflake) {
        collection.deleteOne(ServerConfig::id eq id.value.toLong())
    }

    /**
     *  Checks whether the DB holds a config for a specific server, and if the config is incomplete/incompatible
     *  with the current schema, migrates it and returns the migrated config.
     *  @param id the id of the server
     *  @return whether the DB has the config
     */
    private fun hasConfig(id: Snowflake): Boolean {
        return try {
            collection.findOne(ServerConfig::id eq id.value.toLong()) != null
        } catch (e: MissingKotlinParameterException) {
            migrate(database.db)
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
            Modules.Notifications -> config.notificationsConfig.enabled
            Modules.FilePaste -> config.filePasteConfig.enabled
        }
    }
}
