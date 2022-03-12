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

package io.github.jamalam.pinguino.database.collection

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.mongodb.client.MongoDatabase
import dev.kord.common.entity.Snowflake
import io.github.jamalam.pinguino.Modules
import io.github.jamalam.pinguino.database.entity.ServerConfig
import io.github.jamalam.pinguino.database.getDefault
import io.github.jamalam.pinguino.database.migration.migrate
import io.github.jamalam.pinguino.database.tryOperationUntilSuccess
import io.github.jamalam.pinguino.util.database
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.updateOne

@Suppress("RemoveExplicitTypeArguments")
class ConfigCollection(db: MongoDatabase) : CachedDatabaseCollection<Snowflake, ServerConfig>(db.getCollection<ServerConfig>()) {
    fun getConfig(id: Snowflake): ServerConfig {
        val conf: ServerConfig

        if (!hasConfig(id)) {
            conf = ServerConfig::class.getDefault(id)
            tryOperationUntilSuccess { collection.insertOne(conf) }
            cache[id] = conf
        } else {
            if (!cache.containsKey(id)) {
                conf = tryOperationUntilSuccess { collection.findOne(ServerConfig::id eq id.value.toLong())!! }
                cache[id] = conf
            } else {
                conf = cache[id]!!
            }
        }

        return conf
    }

    fun updateConfig(id: Snowflake, updated: ServerConfig) {
        tryOperationUntilSuccess { collection.updateOne(ServerConfig::id eq id.value.toLong(), updated) }
        cache[id] = updated
    }

    fun deleteConfig(id: Snowflake) {
        tryOperationUntilSuccess { collection.deleteOne(ServerConfig::id eq id.value.toLong()) }
        cache.remove(id)
    }

    private fun hasConfig(id: Snowflake): Boolean {
        try {
            if (cache.containsKey(id)) return true

            return tryOperationUntilSuccess { collection.findOne(ServerConfig::id eq id.value.toLong()) != null }
        } catch (e: MissingKotlinParameterException) {
            tryOperationUntilSuccess { migrate(database.db) }
            hasConfig(id)
        }

        return false
    }

    fun isModuleEnabled(id: Snowflake, module: Modules): Boolean {
        val config = getConfig(id)

        return when (module) {
            Modules.Role -> config.roleConfig.enabled
            Modules.Tags -> config.tagsConfig.enabled
            Modules.Quotes -> config.quotesConfig.enabled
            Modules.Moderation -> config.moderationConfig.enabled
            Modules.Logging -> config.loggingConfig.enabled
            Modules.Notifications -> config.notificationsConfig.enabled
            Modules.FilePaste -> config.filePasteConfig.enabled
            Modules.Phishing -> config.phishingConfig.enabled
        }
    }
}
