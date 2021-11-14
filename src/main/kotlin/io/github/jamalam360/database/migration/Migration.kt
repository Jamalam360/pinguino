package io.github.jamalam360.database.migration

import com.mongodb.client.MongoDatabase
import io.github.jamalam360.database.entity.*
import io.github.jamalam360.database.getDefault
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
    }
}
