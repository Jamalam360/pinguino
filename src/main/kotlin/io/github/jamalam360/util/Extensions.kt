package io.github.jamalam360.util

import com.kotlindiscord.kord.extensions.DISCORD_BLURPLE
import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.DISCORD_YELLOW
import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.jamalam360.extensions.moderation.LoggingExtension
import kotlinx.datetime.Clock

/**
 * @author  Jamalam360
 */

fun ExtensibleBot.getLoggingExtension(): LoggingExtension = this.extensions["logging"] as LoggingExtension

suspend fun GuildBehavior.getLogChannel(): MessageChannel? {
    val conf = database.config.getConfig(this.id)

    if (conf.loggingConfig.enabled && conf.loggingConfig.channel != null) {
        val channel = this.getChannel(Snowflake(conf.loggingConfig.channel!!))

        if (channel.type == ChannelType.GuildText) {
            return channel as MessageChannel
        }
    }

    return null
}

fun EmbedBuilder.setAuthor(user: User) {
    author {
        name = user.username
        icon = user.avatar.url
    }
}

fun EmbedBuilder.now() {
    timestamp = Clock.System.now()
}

fun EmbedBuilder.info() {
    color = DISCORD_BLURPLE
}

fun EmbedBuilder.warn() {
    color = DISCORD_YELLOW
}

fun EmbedBuilder.warnStrong() {
    color = DISCORD_RED
}