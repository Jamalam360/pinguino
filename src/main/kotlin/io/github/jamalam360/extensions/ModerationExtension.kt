package io.github.jamalam360.extensions

import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.withTyping
import dev.kord.core.behavior.edit
import dev.kord.core.event.channel.thread.TextChannelThreadCreateEvent
import io.github.jamalam360.DATABASE
import io.github.jamalam360.SingleRoleArgs
import io.github.jamalam360.database.Modules
import io.github.jamalam360.hasModeratorRole
import io.github.jamalam360.isModuleEnabled
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * @author Jamalam360
 */

@OptIn(KordPreview::class)
class ModerationExtension : Extension() {
    override val name: String = "moderation"

    @OptIn(ExperimentalTime::class)
    override suspend fun setup() {
        //region Slash Commands
        ephemeralSlashCommand {
            name = "moderation"
            description = "Commands for moderators"

            check {
                hasModeratorRole()
                isModuleEnabled(Modules.Moderation)
            }

            group("thread-auto-join") {
                description = "Commands for the management of the thread auto-joiner feature"

                ephemeralSubCommand(::SingleRoleArgs) {
                    name = "add-role"
                    description = "Add a role to the thread auto-join list"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)

                        if (conf.moderationConfig.threadAutoJoinRoles.contains(arguments.role.id.value)) {
                            respond {
                                content = "${arguments.role.mention} is already on the auto-join list!"
                            }
                        } else {
                            conf.moderationConfig.threadAutoJoinRoles.add(arguments.role.id.value)
                            DATABASE.config.updateConfig(guild!!.id, conf)

                            respond {
                                content = "Successfully added ${arguments.role.mention} to the auto-join list"
                            }
                        }
                    }
                }

                ephemeralSubCommand(::SingleRoleArgs) {
                    name = "remove-role"
                    description = "Remove a role from the thread auto-join list"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)

                        if (conf.moderationConfig.threadAutoJoinRoles.contains(arguments.role.id.value)) {
                            conf.moderationConfig.threadAutoJoinRoles.remove(arguments.role.id.value)
                            DATABASE.config.updateConfig(guild!!.id, conf)

                            respond {
                                content = "Successfully removed ${arguments.role.mention} from the auto-join list"
                            }
                        } else {
                            respond {
                                content = "${arguments.role.mention} is not on the auto-join list"
                            }
                        }
                    }
                }
            }
        }
        //endregion

        //region Events
        event<TextChannelThreadCreateEvent> {
            check { failIf(event.channel.member != null) }

            action {
                if (DATABASE.config.getConfig(event.channel.guildId).moderationConfig.threadAutoJoinRoles.isNotEmpty()) {
                    val msg =
                        event.channel.createMessage("Nice thread ${event.channel.owner.mention}! Hold on while I get some people in here!")

                    event.channel.withTyping {
                        delay(Duration.Companion.seconds(3))
                    }

                    var mentions = ""
                    val roles = DATABASE.config.getConfig(event.channel.guildId).moderationConfig.threadAutoJoinRoles

                    for (id in roles) {
                        mentions += "${event.channel.guild.getRole(Snowflake(id)).mention}, "
                    }

                    msg.edit {
                        content = "Hey ${mentions.dropLast(2)} come look at this cool thread!"
                    }

                    event.channel.withTyping {
                        delay(Duration.Companion.seconds(3))
                    }

                    msg.edit {
                        content = "Welcome to your thread ${event.channel.owner.mention}, enjoy!"
                    }
                } else {
                    event.channel.createMessage("Welcome to your thread ${event.channel.owner.mention}, enjoy!")
                }
            }
        }
        //endregion
    }
}
