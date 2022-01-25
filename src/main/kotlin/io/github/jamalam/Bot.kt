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
import io.github.jamalam.extensions.bot.BotUtilityExtension
import io.github.jamalam.extensions.moderation.*
import io.github.jamalam.extensions.user.*
import io.github.jamalam.util.*

suspend fun main() {
    val bot = ExtensibleBot(TOKEN) {
        applicationCommands {
            if (!PRODUCTION) {
                defaultGuild(TEST_SERVER_ID)
            }
        }

        extensions {
            add(::QuoteExtension)
            add(::BotUtilityExtension)
            add(::ModuleExtension)
            add(::LoggingExtension)
            add(::ModerationExtension)
            add(::ModeratorUtilityExtension)
            add(::UserUtilityExtension)
            add(::FunExtension)
            add(::TagExtension)
            add(::NotificationsExtension)
            add(::FilePasteExtension)

            help {
                enableBundledExtension = false
            }

            if (PRODUCTION) {
                sentry {
                    enable = true
                    dsn = SENTRY_URL
                    environment = "Production"
                    distribution = VERSION //TODO: Use release instead of distribution - waiting on fix on KordEx's side
                }
            }
        }
    }

    bot.start()
}
