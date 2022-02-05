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

package io.github.jamalam.database.migration

import com.mongodb.client.MongoDatabase
import io.github.jamalam.database.entity.*
import io.github.jamalam.database.getDefault
import org.litote.kmongo.exists
import org.litote.kmongo.getCollection
import org.litote.kmongo.setValue

/**
 * Migrates old versions of ServerConfig's to newer schemas, filling in missing values with defaults.
 * @author  Jamalam360
 */

fun migrate(db: MongoDatabase) {
    with(db.getCollection<ServerConfig>()) {
        updateMany(
            ServerConfig::quotesConfig exists false,
            setValue(
                ServerConfig::quotesConfig, ServerQuotesConfig::class.getDefault()
            )
        )

        updateMany(
            ServerConfig::loggingConfig exists false,
            setValue(
                ServerConfig::loggingConfig, ServerLoggingConfig::class.getDefault()
            )
        )

        updateMany(
            ServerConfig::moderationConfig exists false,
            setValue(
                ServerConfig::moderationConfig, ServerModerationConfig::class.getDefault()
            )
        )

        updateMany(
            ServerConfig::tagsConfig exists false,
            setValue(
                ServerConfig::tagsConfig, ServerTagsConfig::class.getDefault()
            )
        )

        updateMany(
            ServerConfig::notificationsConfig exists false,
            setValue(
                ServerConfig::notificationsConfig, ServerNotificationsConfig::class.getDefault()
            )
        )

        updateMany(
            ServerConfig::filePasteConfig exists false,
            setValue(
                ServerConfig::filePasteConfig, ServerFilePasteConfig::class.getDefault()
            )
        )

        updateMany(
            ServerConfig::phishingConfig exists false,
            setValue(
                ServerConfig::phishingConfig, ServerPhishingConfig::class.getDefault()
            )
        )
    }
}
