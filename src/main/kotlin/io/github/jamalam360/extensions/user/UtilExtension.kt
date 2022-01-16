package io.github.jamalam360.extensions.user

import com.kotlindiscord.kord.extensions.DISCORD_GREEN
import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.DISCORD_YELLOW
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.checks.isInThread
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.threads.edit
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.core.event.channel.thread.ThreadUpdateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam360.DATABASE
import io.github.jamalam360.PINGUINO_PFP
import io.github.jamalam360.getLoggingExtension
import io.github.jamalam360.hasModeratorRole
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable

/**
 * Random commands that don't fit elsewhere.
 * @author  Jamalam360
 */

@SuppressWarnings("MaxLineLength")
@OptIn(KordPreview::class)
class UtilExtension : Extension() {
    override val name: String = "util"
    private val scheduler = Scheduler()

    private val client = HttpClient {
        install(JsonFeature)
    }

    @Suppress("DuplicatedCode")
    override suspend fun setup() {
        event<ThreadUpdateEvent> {
            action {
                if (event.channel.isArchived && DATABASE.savedThreads.shouldSave(event.channel.id)) {
                    event.channel.edit {
                        archived = false
                        reason = "Preventing thread from being archived"
                    }
                }
            }
        }

        ephemeralSlashCommand {
            name = "invite"
            description = "Get an invite link for Pinguino!"

            action {
                val embed = EmbedBuilder()
                embed.image = PINGUINO_PFP
                embed.title = "Invite Pinguino!"
                embed.description = "Click [here]" +
                        "(https://discord.com/api/oauth2/authorize?client_id=896758540784500797&permissions=8&scope=bot%20applications.commands)" +
                        " to invite Pinguino to your own server"

                respond {
                    embeds.add(embed)
                }
            }
        }

        ephemeralSlashCommand {
            name = "thread"
            description = "Commands to manage threads"

            ephemeralSubCommand(::ThreadArchiveArgs) {
                name = "archive"
                description = "Archive the thread you are in, if you have permission"

                check {
                    isInThread()
                }

                action {
                    val channel = channel.asChannel() as ThreadChannel
                    val roles = user.asMember(guild!!.id).roles.toList()
                    val modRole = Snowflake(DATABASE.config.getConfig(guild!!.id).moderationConfig.moderatorRole)

                    if (roles.contains(guild!!.getRoleOrNull(modRole)) || channel.ownerId == user.id
                    ) {
                        if (!channel.isArchived) {
                            if (roles.contains(guild!!.getRoleOrNull(modRole)) && arguments.lock == true) {
                                channel.edit {
                                    locked = true
                                    this.archived = true
                                    reason = "Thread archived and locked by ${user.mention}"
                                }

                                respond {
                                    content = "Successfully archived and locked thread"
                                }
                            } else if (arguments.lock == true) {
                                channel.edit {
                                    this.archived = true
                                    reason = "Archived by ${user.mention}"
                                }

                                respond {
                                    content =
                                        "Successfully archived thread, but you do not have permission to lock this thread"
                                }
                            } else {
                                channel.edit {
                                    this.archived = true
                                    reason = "Archived by ${user.mention}"
                                }

                                respond {
                                    content = "Successfully archived thread"
                                }
                            }
                        } else {
                            respond {
                                content = "This thread is already archived"
                            }
                        }

                        bot.getLoggingExtension().logAction(
                            "Thread archived",
                            if (roles.contains(guild!!.getRoleOrNull(modRole)) && arguments.lock!!) "Locked" else "Not Locked",
                            user.asUser(),
                            guild!!.asGuild()
                        )
                    } else {
                        respond {
                            content = "You do not have permission to archive or lock this thread"
                        }
                    }
                }
            }

            ephemeralSubCommand(::ThreadRenameArgs) {
                name = "rename"
                description = "Rename the thread you are in, if you have permission"

                check {
                    isInThread()
                }

                action {
                    val channel = channel.asChannel() as ThreadChannel
                    val roles = user.asMember(guild!!.id).roles.toList()
                    val modRole = Snowflake(DATABASE.config.getConfig(guild!!.id).moderationConfig.moderatorRole)

                    if (roles.contains(guild!!.getRoleOrNull(modRole)) || channel.ownerId == user.id) {
                        val before = channel.name

                        channel.edit {
                            this.name = arguments.name
                            reason = "Renamed by ${user.mention}"
                        }

                        respond {
                            content = "Successfully renamed thread"
                        }

                        bot.getLoggingExtension().logAction(
                            "Thread renamed",
                            "'$before' --> '${arguments.name}",
                            user.asUser(),
                            guild!!.asGuild()
                        )
                    } else {
                        respond {
                            content = "You do not have permission to rename this thread"
                        }
                    }
                }
            }

            ephemeralSubCommand(::ThreadSaveArgs) {
                name = "save"
                description = "Prevent the thread you are in from archiving, if you have permission"

                check {
                    hasModeratorRole()
                    isInThread()
                }

                action {
                    if (arguments.save) {
                        DATABASE.savedThreads.setSave(channel.id)
                    } else {
                        DATABASE.savedThreads.setSave(channel.id, false)
                    }

                    bot.getLoggingExtension().logAction(
                        if (arguments.save) "Thread Saved" else "Thread Unsaved",
                        channel.mention,
                        user.asUser(),
                        guild!!.asGuild()
                    )

                    respond {
                        content =
                            "Successfully ${if (arguments.save) "set thread to be saved" else "set thread to not be saved"}"
                    }
                }
            }
        }

        ephemeralSlashCommand(::EmbedCreateArgs) {
            name = "embed"
            description = "Post a customised embed"

            check {
                hasModeratorRole()
            }

            action {
                val channel: MessageChannel = if (arguments.channel == null) {
                    channel.asChannel()
                } else {
                    arguments.channel!!.asChannel() as MessageChannel
                }

                if (arguments.delay == null) {
                    channel.createEmbed {
                        this.title = arguments.title
                        this.description = arguments.description
                        this.image = arguments.image
                        this.author = EmbedBuilder.Author()

                        if (arguments.author != null) {
                            this.author!!.name = arguments.author!!.username
                            this.author!!.icon = arguments.author!!.avatar.url
                        }
                    }

                    respond {
                        content = "Embed sent!"
                    }
                } else {
                    scheduler.schedule(arguments.delay!!.seconds.toLong()) {
                        channel.createEmbed {
                            this.title = arguments.title
                            this.description = arguments.description
                            this.image = arguments.image
                            this.author = EmbedBuilder.Author()

                            if (arguments.author != null) {
                                this.author!!.name = arguments.author!!.username
                                this.author!!.icon = arguments.author!!.avatar.url
                            }
                        }
                    }

                    respond {
                        content = "Embed scheduled!"
                    }
                }
            }
        }

        ephemeralSlashCommand(::ScheduleMessageArgs) {
            name = "schedule"
            description = "Schedule a message to be sent"

            check {
                hasModeratorRole()
            }

            action {
                scheduler.schedule(arguments.delay.seconds.toLong()) {
                    (arguments.channel.asChannel() as MessageChannel).createMessage(arguments.message)
                }

                respond {
                    content = "Message scheduled!"
                }
            }
        }

        ephemeralSlashCommand {
            name = "help"
            description = "Get a link to the help page"

            action {
                val embed = EmbedBuilder()
                embed.image = PINGUINO_PFP
                embed.title = "Learn how to use Pinguino!"
                embed.description = "Click [here]" +
                        "(https://github.com/JamCoreDiscord/Pinguino/wiki)" +
                        " to learn about Pinguino's features and commands. " +
                        "If you have any issues or further questions, join the" +
                        " [support server](https://discord.gg/88PWg5TySd)"

                respond {
                    embeds.add(embed)
                }
            }
        }

        ephemeralSlashCommand {
            name = "bugs"
            description = "Get a link to the bug tracker"

            action {
                val embed = EmbedBuilder()
                embed.image = PINGUINO_PFP
                embed.title = "Report bugs with Pinguino"
                embed.description = "Click [here]" +
                        "(https://github.com/JamCoreDiscord/Pinguino/issues)" +
                        " to report bugs with Pinguino. Reports are appreciated " +
                        "and we will get to your report ASAP."

                respond {
                    embeds.add(embed)
                }
            }
        }

        ephemeralSlashCommand(::EchoArgs) {
            name = "echo"
            description = "Echo a message to a channel, or the current channel is no channel is specified"

            check {
                hasModeratorRole()
            }

            action {
                val channel: MessageChannel = if (arguments.channel == null) {
                    channel.asChannel()
                } else {
                    arguments.channel!!.asChannel() as MessageChannel
                }

                channel.createMessage(arguments.message)

                bot.getLoggingExtension().logAction(
                    "/echo Command Used",
                    "Echoed ${arguments.message} to ${channel.mention}",
                    user.asUser(),
                    guild!!.asGuild()
                )

                respond {
                    content = "Message sent!"
                }
            }
        }

        ephemeralSlashCommand(::AskArgs) {
            name = "ask"
            description = "Ask a yes/no question!"

            check {
                hasModeratorRole()
            }

            action {
                val channel: MessageChannel = if (arguments.channel == null) {
                    channel.asChannel()
                } else {
                    arguments.channel!!.asChannel() as MessageChannel
                }

                val message = channel.createEmbed {
                    this.title = arguments.string
                    this.author = EmbedBuilder.Author()
                    this.author!!.name = user.asUser().username
                    this.author!!.icon = user.asUser().avatar.url
                }

                message.addReaction(ReactionEmoji.Unicode("\uD83D\uDC4D"))
                message.addReaction(ReactionEmoji.Unicode("\uD83D\uDC4E"))

                bot.getLoggingExtension().logAction(
                    "/ask Command Used",
                    arguments.string,
                    user.asUser(),
                    guild!!.asGuild()
                )

                respond {
                    content = "Vote created!"
                }
            }
        }

        ephemeralSlashCommand {
            name = "delete-config"
            description = "Delete this servers config from the Pinguino database"

            check {
                hasModeratorRole()
                hasPermission(Permission.Administrator)
            }

            action {
                DATABASE.config.deleteConfig(guild!!.id)

                respond {
                    content = "Config Deleted"
                }

                bot.getLoggingExtension().logAction(
                    "!! Config deleted !!",
                    "Pinguino bot config deleted!",
                    user.asUser(),
                    guild!!.asGuild(),
                    DISCORD_RED
                )
            }
        }

        publicSlashCommand {
            name = "leave"
            description = "Make Pinguino leave the server :("

            check {
                hasModeratorRole()
                hasPermission(Permission.Administrator)
            }

            action {
                DATABASE.config.deleteConfig(guild!!.id)

                respond {
                    content = "Goodbye :wave:"
                }

                bot.getLoggingExtension().logAction(
                    "Pinguino Leaving",
                    "Goodbye! If you had a specific issue with the bot, please report it on the GitHub repository",
                    user.asUser(),
                    guild!!.asGuild(),
                    DISCORD_YELLOW
                )

                guild!!.leave()
            }
        }

        ephemeralSlashCommand(::SingleLinkArgs) {
            name = "shorten-link"
            description = "Shorten a link"

            action {
                client.put<LinkAPIResponse> {
                    url("https://link.jamalam.tech/api/link")
                    contentType(ContentType.Application.Json)
                    body = "{\"link\": \"${arguments.link}\"}"
                }.let {
                    respond {
                        embed {
                            title = "Shortened Link"
                            url = it.link
                            color = DISCORD_GREEN
                        }
                    }
                }
            }
        }

        ephemeralSlashCommand {
            name = "paste"
            description = "Upload a file to hastebin"

            //TODO: Better descriptions

            ephemeralSubCommand(::SingleLinkArgs) {
                name = "url"
                description = "Use a cdn.discordapp.com link to paste your file"

                action {
                    client.post<HastebinApiResponse>("https://www.toptal.com/developers/hastebin/documents") {
                        body = String(
                            client.get<HttpResponse>(arguments.link).content.toInputStream()
                                .readAllBytes()
                        )
                    }.let { hastebinApiResponse ->
                        respond {
                            embed {
                                title = "File Uploaded to Hastebin"
                                url =
                                    "https://www.toptal.com/developers/hastebin/${hastebinApiResponse.key}"
                                color = DISCORD_GREEN
                            }
                        }
                    }
                }
            }

            ephemeralSubCommand(::SingleStringArgs) {
                name = "typed"
                description = "Type out your file into the slash command arguments"

                action {
                    client.post<HastebinApiResponse>("https://www.toptal.com/developers/hastebin/documents") {
                        body = arguments.string
                    }.let { hastebinApiResponse ->
                        respond {
                            embed {
                                title = "File Uploaded to Hastebin"
                                url =
                                    "https://www.toptal.com/developers/hastebin/${hastebinApiResponse.key}"
                                color = DISCORD_GREEN
                            }
                        }
                    }
                }
            }
        }
    }

    @Serializable
    data class HastebinApiResponse(val key: String)

    @Serializable
    data class LinkAPIResponse(val link: String)

    //region Arguments
    inner class AskArgs : Arguments() {
        val string by string(
            "question",
            "The question to ask"
        )
        val channel by optionalChannel(
            "channel",
            "The channel to send the message to, or the current one if unspecified"
        )
    }

    inner class ThreadRenameArgs : Arguments() {
        val name by string(
            "name",
            "The threads new name"
        )
    }

    inner class ThreadSaveArgs : Arguments() {
        val save by boolean(
            "save",
            "Whether or not to prevent the thread from archiving"
        )
    }

    inner class EchoArgs : Arguments() {
        val message by string(
            "message",
            "The message to be sent"
        )
        val channel by optionalChannel(
            "channel",
            "The channel to send the message to, or the current one if unspecified"
        )
    }

    inner class EmbedCreateArgs : Arguments() {
        val channel by optionalChannel(
            "channel",
            "The channel to send the message to, or the current one if unspecified"
        )
        val delay by optionalDuration(
            "delay",
            "The time until the embed should be sent - optional"
        )
        val title by optionalString(
            "title",
            "The title of the embed"
        )
        val description by optionalString(
            "description",
            "The description of the embed"
        )
        val image by optionalString(
            "image-url",
            "The URL of the image of the embed"
        )
        val author by optionalUser(
            "author",
            "The author"
        )
    }

    inner class ScheduleMessageArgs : Arguments() {
        val channel by channel(
            "channel",
            "The channel to send the message to"
        )
        val delay by duration(
            "duration",
            "The time until the message should be sent"
        )
        val message by string(
            "message",
            "The message to send"
        )
    }

    inner class ThreadArchiveArgs : Arguments() {
        val lock by optionalBoolean(
            "lock",
            "Whether to lock the thread as well, if you are a moderator"
        )
    }

    inner class SingleLinkArgs : Arguments() {
        val link by string(
            "link",
            "The link"
        )
    }

    inner class SingleStringArgs : Arguments() {
        val string by string(
            "string",
            "The string"
        )
    }
    //endregion
}

