package io.github.jamalam360

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake
import io.github.jamalam360.database.Database
import io.github.jamalam360.extensions.bot.BotUtilityExtension
import io.github.jamalam360.extensions.moderation.LoggingExtension
import io.github.jamalam360.extensions.moderation.ModerationExtension
import io.github.jamalam360.extensions.moderation.ModuleExtension
import io.github.jamalam360.extensions.moderation.NotificationsExtension
import io.github.jamalam360.extensions.user.FunExtension
import io.github.jamalam360.extensions.user.QuoteExtension
import io.github.jamalam360.extensions.user.TagExtension
import io.github.jamalam360.extensions.user.UtilExtension

//region ENV Variables
val TEST_SERVER_ID = Snowflake(
    env("TEST_SERVER_ID").toLong()
)
val PRODUCTION = env("PRODUCTION").toBoolean()
private val TOKEN = if (PRODUCTION) {
    env("TOKEN")
} else {
    env("TEST_BOT_TOKEN")
}
val ERROR_WEBHOOK_URL = env("ERROR_WEBHOOK_URL")
val DBL_TOKEN = env("DBL_TOKEN")
//endregion

//region Constant Values
const val PINGUINO_PFP = "https://images-ext-2.discordapp.net/external/tM2ezTNgh6TK_9IW5eCGQLtuaarLJfjdRgJ3hmRQ5rs" +
        "/%3Fsize%3D256/https/cdn.discordapp.com/avatars/896758540784500797/507601ac" +
        "31f51ffc334fac125089f7ea.png"

const val VERSION = "v0.3.5"
const val DBL_URL = "https://top.gg/api/bots/896758540784500797/stats"
//endregion

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
            add(::BotUtilityExtension)
            add(::ModuleExtension)
            add(::LoggingExtension)
            add(::ModerationExtension)
            add(::UtilExtension)
            add(::FunExtension)
            add(::TagExtension)
            add(::NotificationsExtension)
        }
    }

    bot.start()
}
