package io.github.jamalam360.pinguino.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import io.github.jamalam360.pinguino.config.parse.ConfigLoader

/**
 * @author  Jamalam
 */

val config = ConfigLoader.load("../config")

suspend fun main() {
    val bot = ExtensibleBot(config.bot().credentials.discordToken) {
        applicationCommands {
            defaultGuild(config.bot().administration.serverId)
        }

        extensions {
            //TODO(Jamalam360): Do this with reflections to prevent forgetting to add new extensions
            add(::TestExtension)
        }
    }

    bot.start()
}
