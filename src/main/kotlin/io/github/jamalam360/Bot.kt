package io.github.jamalam360

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake
import io.github.jamalam360.extensions.TestExtension

val TEST_SERVER_ID = Snowflake(
    env("TEST_SERVER_ID").toLong()
)

val PRODUCTION = env("PRODUCTION").toBoolean()

private val TOKEN = if (PRODUCTION) {
    env("TOKEN")
} else {
    env("TEST_BOT_TOKEN")
}

suspend fun main() {
    val bot = ExtensibleBot(TOKEN) {
        extensions {
            add(::TestExtension)
        }
    }

    bot.start()
}
