package io.github.jamalam360

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake
import io.github.jamalam360.database.Database
import io.github.jamalam360.extensions.*

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
const val PINGUINO_PFP = "https://images-ext-2.discordapp.net/external/tM2ezTNgh6TK_9IW5eCGQLtuaarLJfjdRgJ3hmRQ5rs" +
        "/%3Fsize%3D256/https/cdn.discordapp.com/avatars/896758540784500797/507601ac" +
        "31f51ffc334fac125089f7ea.png"

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
            add(::LoggingExtension)
            add(::ModerationExtension)
            add(::UtilExtension)
            add(::FunExtension)
        }
    }

    bot.start()
}
