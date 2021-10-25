package io.github.jamalam360.database.migration

import com.mongodb.client.MongoDatabase
import io.github.jamalam360.database.ServerConfig
import io.github.jamalam360.database.ServerLoggingConfig
import io.github.jamalam360.database.ServerModerationConfig
import io.github.jamalam360.database.ServerQuotesConfig
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
                ServerConfig::quotesConfig, ServerQuotesConfig(
                    true,
                    null,
                    true
                )
            )
        )

        updateMany(
            ServerConfig::loggingConfig exists false,
            setValue(
                ServerConfig::loggingConfig, ServerLoggingConfig(
                    true,
                    null
                )
            )
        )

        updateMany(
            ServerConfig::moderationConfig exists false,
            setValue(
                ServerConfig::moderationConfig, ServerModerationConfig(
                    enabled = true,
                    logActions = true,
                    moderatorRole = 0,

                    mutableListOf()
                )
            )
        )
    }
}
