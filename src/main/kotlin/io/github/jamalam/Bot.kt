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

package io.github.jamalam

import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam.config.config
import io.github.jamalam.database.migration.migrate
import io.github.jamalam.extensions.bot.AnnouncementExtension
import io.github.jamalam.extensions.bot.BotUtilityExtension
import io.github.jamalam.extensions.moderation.*
import io.github.jamalam.extensions.user.*
import io.github.jamalam.util.*

suspend fun main() {
    BOOT_TIME // init this field
    config.validate()
    migrate(database.db)

    val bot = ExtensibleBot(config.token()) {
        applicationCommands {
            if (!config.production()) {
                defaultGuild(config.development?.serverId)
            }
        }

        errorResponse { message, _ ->
            embed {
                title = "Error"
                description = message
                pinguino()
                error()
                now()
                footer {
                    text = "Found a bug? Report it using `/help`"
                }
            }
        }

        extensions {
            add(::AnnouncementExtension)
            add(::BotUtilityExtension)
            add(::FilePasteExtension)
            add(::FunExtension)
            add(::LoggingExtension)
            add(::ModerationExtension)
            add(::ModeratorUtilityExtension)
            add(::ModuleExtension)
            add(::NotificationsExtension)
            add(::PhishingExtension)
            add(::QuoteExtension)
            add(::RoleExtension)
            add(::TagExtension)
            add(::UserUtilityExtension)

            help {
                enableBundledExtension = false
            }

            if (config.production() && config.auth.sentryUrl != null) {
                sentry {
                    enable = true
                    dsn = config.auth.sentryUrl
                    environment = "Production"
                    release = VERSION
                }
            }
        }

        hooks {
            beforeStart {
                logger.info { "Connecting to Discord..." }
            }
        }
    }

    bot.start()
}
