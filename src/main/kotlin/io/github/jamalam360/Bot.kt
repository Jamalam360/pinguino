package io.github.jamalam360

import com.kotlindiscord.kord.extensions.ExtensibleBot
import io.github.jamalam360.extensions.bot.BotUtilityExtension
import io.github.jamalam360.extensions.moderation.LoggingExtension
import io.github.jamalam360.extensions.moderation.ModerationExtension
import io.github.jamalam360.extensions.moderation.ModuleExtension
import io.github.jamalam360.extensions.moderation.NotificationsExtension
import io.github.jamalam360.extensions.user.*
import io.github.jamalam360.util.*

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
            add(::UtilExtension)
            add(::FunExtension)
            add(::TagExtension)
            add(::NotificationsExtension)
            add(::FilePasteExtension)

            help {
                enableBundledExtension = false
            }

            sentry {
                enable = true
                dsn = SENTRY_URL

                environment = if (PRODUCTION) {
                    "production"
                } else {
                    "testing"
                }

                distribution = VERSION
            }
        }
    }

    bot.start()
}
