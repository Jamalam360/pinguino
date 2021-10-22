package io.github.jamalam360.extensions

import com.kotlindiscord.kord.extensions.checks.isInThread
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.threads.edit
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.jamalam360.DATABASE
import kotlinx.coroutines.flow.toList

/**
 * Random commands that don't fit elsewhere.
 * @author  Jamalam360
 */

@SuppressWarnings("MaxLineLength")
@OptIn(KordPreview::class)
class UtilExtension : Extension() {
    override val name: String = "util"

    override suspend fun setup() {
        ephemeralSlashCommand {
            name = "invite"
            description = "Get an invite link for Pinguino!"

            action {
                val embed = EmbedBuilder()
                // TODO: Update invite link when bot goes public
                embed.image =
                    "https://images-ext-2.discordapp.net/external/tM2ezTNgh6TK_9IW5eCGQLtuaarLJfjdRgJ3hmRQ5rs" +
                            "/%3Fsize%3D256/https/cdn.discordapp.com/avatars/896758540784500797/507601ac" +
                            "31f51ffc334fac125089f7ea.png"
                embed.title = "Invite Pinguino!"
                embed.description = "Click [here](https://google.com/) to invite Pinguino to your own server"

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

                if (roles.contains(guild!!.getRoleOrNull(modRole)) || channel.ownerId == user.id
                ) {
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
    }

    //region Arguments
    inner class ThreadRenameArgs : Arguments() {
        val name by string(
            "name",
            "The threads new name"
        )
    }
    //endregion
}
