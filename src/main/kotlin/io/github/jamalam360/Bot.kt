package io.github.jamalam360

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake
import io.github.jamalam360.database.Database
import io.github.jamalam360.extensions.BotStatusExtension
import io.github.jamalam360.extensions.ModuleExtension
import io.github.jamalam360.extensions.QuoteExtension

val TEST_SERVER_ID = Snowflake(
    env("TEST_SERVER_ID").toLong()
)

val PRODUCTION = env("PRODUCTION").toBoolean()

private val TOKEN = if (PRODUCTION) {
    env("TOKEN")
} else {
    env("TEST_BOT_TOKEN")
}

val DATABASE = Database()

suspend fun main() {
    val bot = ExtensibleBot(TOKEN) {
        applicationCommands {
            if (!PRODUCTION) {
                defaultGuild(TEST_SERVER_ID)
            }
        }

        extensions {
            add(::QuoteExtension)
            add(::BotStatusExtension)
            add(::ModuleExtension)
        }
    }

    bot.start()
}
