package io.github.jamalam360.extensions

import com.kotlindiscord.kord.extensions.checks.isInThread
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.threads.edit
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.jamalam360.DATABASE
import io.github.jamalam360.PINGUINO_PFP
import io.github.jamalam360.hasModeratorRole
import kotlinx.coroutines.flow.toList

/**
 * Random commands that don't fit elsewhere.
 * @author  Jamalam360
 */

@SuppressWarnings("MaxLineLength")
@OptIn(KordPreview::class)
class UtilExtension : Extension() {
    override val name: String = "util"
    private val scheduler = Scheduler()

    override suspend fun setup() {
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
                        channel.edit {
                            this.archived = true
                            reason = "Archived by ${user.mention}"
                        }

                        respond {
                            content = "Successfully archived thread"
                        }
                    } else {
                        respond {
                            content = "This thread is already archived"
                        }
                    }

                    (bot.extensions["logging"] as LoggingExtension).logAction(
                        "Thread archived",
                        "",
                        user.asUser(),
                        guild!!.asGuild()
                    )
                } else {
                    respond {
                        content = "You do not have permission to archive this thread"
                    }
                }
            }
        }

        ephemeralSlashCommand(::ThreadRenameArgs) {
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

                    (bot.extensions["logging"] as LoggingExtension).logAction(
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

        ephemeralSlashCommand(::EmbedCreateArgs) {
            name = "embed"
            description = "Post a customised embed"

            check {
                hasModeratorRole()
            }

            action {
                (arguments.channel.asChannel() as MessageChannel).createEmbed {
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
    }

    //region Arguments
    inner class ThreadRenameArgs : Arguments() {
        val name by string(
            "name",
            "The threads new name"
        )
    }

    inner class EmbedCreateArgs : Arguments() {
        val channel by channel(
            "channel",
            "The channel to send the embed to"
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
//endregion
}
