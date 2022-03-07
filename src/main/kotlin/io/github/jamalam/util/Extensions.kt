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

@file:Suppress("DuplicatedCode")

package io.github.jamalam.util

import com.kotlindiscord.kord.extensions.DISCORD_BLURPLE
import com.kotlindiscord.kord.extensions.DISCORD_GREEN
import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.utils.getJumpUrl
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.Role
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam.database.entity.ServerConfig
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.toDateTimePeriod
import kotlin.random.Random
import kotlin.time.ExperimentalTime

/**
 * @author  Jamalam360
 */

suspend fun GuildBehavior.getLogChannel(): MessageChannel? {
    val conf = database.serverConfig.getConfig(this.id)

    if (
        conf.loggingConfig.enabled &&
        conf.loggingConfig.channel != null &&
        conf.loggingConfig.channel != 0L
    ) {
        val channel = this.getChannel(Snowflake(conf.loggingConfig.channel!!))

        if (channel.type == ChannelType.GuildText) {
            return channel as MessageChannel
        }
    }

    return null
}

suspend fun GuildBehavior.getPublicModLogChannel(): MessageChannel? {
    val conf = this.getConfig()

    if (
        conf.moderationConfig.enabled &&
        conf.moderationConfig.publicModLogChannel != null &&
        conf.moderationConfig.publicModLogChannel != 0L
    ) {
        val channel = this.getChannel(Snowflake(conf.moderationConfig.publicModLogChannel!!))

        if (channel.type == ChannelType.GuildText) {
            return channel as MessageChannel
        }
    }

    return null
}

fun GuildBehavior.getConfig(): ServerConfig = database.serverConfig.getConfig(this.id)

fun ThreadChannelBehavior.save(save: Boolean = true) {
    if (save) {
        database.savedThreads.setSave(this.id, false)
    } else {
        database.savedThreads.setSave(this.id, false)
    }
}

fun EmbedBuilder.userAuthor(user: User?) {
    if (user != null) {
        author {
            name = user.username
            icon = user.avatar!!.url
        }
    }
}

fun EmbedBuilder.pinguino() {
    author {
        name = "Pinguino"
        icon = PINGUINO_PFP
    }
}

fun EmbedBuilder.info(message: String) {
    title = message
}

fun EmbedBuilder.image(url: String?) {
    image = url
}

fun EmbedBuilder.userField(name: String, user: User?) {
    if (user != null) {
        field {
            this.name = name
            this.value = "`${user.id.value}` / ${user.username}"
        }
    }
}

fun EmbedBuilder.channelField(name: String, channel: Channel?) {
    if (channel != null) {
        field {
            this.name = name
            this.value = "`${channel.id.value}` / ${channel.mention}"
        }
    }
}

fun EmbedBuilder.roleField(name: String, role: Role?) {
    if (role != null) {
        field {
            this.name = name
            this.value = "`${role.id.value}` / ${role.mention}"
        }
    }
}

fun EmbedBuilder.stringField(name: String, value: String?) {
    if (value != null) {
        field {
            this.name = name
            this.value = value
        }
    }
}

fun EmbedBuilder.messageLinkField(name: String, value: Message?) {
    if (value != null) {
        field {
            this.name = name
            this.value = "`${value.id}` / [Jump Link](${value.getJumpUrl()})"
        }
    }
}

suspend fun EmbedBuilder.message(value: Message?, authorAsField: Boolean = false) {
    if (value != null) {
        if (authorAsField) {
            userField("Message Author", value.author)
        } else {
            userAuthor(value.author!!)
        }
        stringField("Message Content", value.content.ifEmpty { "Empty" })
        messageLinkField("Message Information", value)
        channelField("Channel", value.channel.asChannel())
    }
}

fun EmbedBuilder.now() {
    timestamp = Clock.System.now()
}

fun EmbedBuilder.log() {
    color = DISCORD_BLURPLE
}

fun EmbedBuilder.success() {
    color = DISCORD_GREEN
}

fun EmbedBuilder.error() {
    color = DISCORD_RED
}

fun DateTimePeriod.toPrettyString(): String {
    var result = ""

    if (this.years > 0) {
        result += "${this.years} year${if (this.years > 1) "s" else ""}"
    }

    if (this.months > 0) {
        result += "${if (result.isNotEmpty()) ", " else ""}${this.months} month${if (this.months > 1) "s" else ""}"
    }

    if (this.days > 0) {
        result += "${if (result.isNotEmpty()) ", " else ""}${this.days} day${if (this.days > 1) "s" else ""}"
    }

    if (this.hours > 0) {
        result += "${if (result.isNotEmpty()) ", " else ""}${this.hours} hour${if (this.hours > 1) "s" else ""}"
    }

    if (this.minutes > 0) {
        result += "${if (result.isNotEmpty()) ", " else ""}${this.minutes} minute${if (this.minutes > 1) "s" else ""}"
    }

    if (this.seconds > 0) {
        result += "${if (result.isNotEmpty()) ", " else ""}${this.seconds} second${if (this.seconds > 1) "s" else ""}"
    }

    val start = result.lastIndexOf(",")

    if (start != -1) {
        var newResult = result.substring(0, start)
        newResult += " and "
        newResult += result.substring(start + 2)
        result = newResult
    }

    return result
}

fun DateTimePeriod.toSeconds(): Long {
    @Suppress("UnnecessaryParentheses", "UnderscoresInNumericLiterals")
    return (years * 31536000L) + (months * 2678400L) + (days * 86400L) + (hours * 3600) + (minutes * 60) + seconds
}

@Suppress("unused")
@OptIn(ExperimentalTime::class)
fun Kord.getUptime(): DateTimePeriod = (Clock.System.now() - BOOT_TIME).toDateTimePeriod()

fun Random.Default.nextHex(): String {
    val hexDigits = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F")
    return hexDigits[nextInt(0, hexDigits.size)]
}

suspend fun Message.forward(channel: MessageChannel) {
    channel.createMessage {
        content = this@forward.content

        this@forward.embeds.forEach { forwardEmbed ->
            embed {
                title = forwardEmbed.title
                description = forwardEmbed.description
                color = forwardEmbed.color
                timestamp = forwardEmbed.timestamp

                if (forwardEmbed.author != null) {
                    author {
                        name = forwardEmbed.author!!.name
                        icon = forwardEmbed.author!!.iconUrl
                        url = forwardEmbed.author!!.url
                    }
                }

                forwardEmbed.fields.forEach { forwardField ->
                    field {
                        name = forwardField.name
                        value = forwardField.value
                        inline = forwardField.inline
                    }
                }
            }
        }
    }
}
