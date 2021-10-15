package io.github.jamalam360.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.boolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.commands.converters.impl.role
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordPreview
import io.github.jamalam360.DATABASE
import io.github.jamalam360.hasModeratorRole

/**
 * @author  Jamalam360
 */

@OptIn(KordPreview::class)
class ModuleExtension : Extension() {
    override val name: String = "modules"

    //region Module Names
    private val quotesModule: String = "the Quotes module"
    private val loggingModule: String = "the Logging module"
    private val moderationModule: String = "the Moderation module"
    //endregion

    @Suppress("DuplicatedCode")
    override suspend fun setup() {
        //region Slash Commands
        publicSlashCommand {
            name = "module"
            description = "Alter the settings of a specific module"

            check {
                hasModeratorRole()
            }

            //region Quotes Module
            group("quotes") {
                description = "Alter the settings of $quotesModule"

                publicSubCommand {
                    name = "enable"
                    description = "Enable $quotesModule"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.quotesConfig.enabled = true
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        respond {
                            content = "Successfully enabled $quotesModule"
                        }
                    }
                }

                publicSubCommand {
                    name = "disable"
                    description = "Disable $quotesModule"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.quotesConfig.enabled = false
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        respond {
                            content = "Successfully disabled $quotesModule"
                        }
                    }
                }

                publicSubCommand(::SingleChannelArgs) {
                    name = "set-channel"
                    description = "Set the channel quote embeds will be sent to"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.quotesConfig.channel = arguments.channel.id.value
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        respond {
                            content = "Successfully set Quotes channel to ${arguments.channel.mention}"
                        }
                    }
                }

                publicSubCommand(::SingleBooleanArgs) {
                    name = "enable-logging"
                    description = "Whether to log quotes in the moderator log channel"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.quotesConfig.log = arguments.boolean
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        respond {
                            content = if (arguments.boolean) {
                                "Successfully enabled logging for $quotesModule"
                            } else {
                                "Successfully disabled logging for $quotesModule"
                            }
                        }
                    }
                }
            }
            //endregion

            //region Logging Module
            group("logging") {
                description = "Alter the settings of $loggingModule"

                publicSubCommand {
                    name = "enable"
                    description = "Enable $loggingModule"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.loggingConfig.enabled = true
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        respond {
                            content = "Successfully enabled $loggingModule"
                        }
                    }
                }

                publicSubCommand {
                    name = "disable"
                    description = "Disable $loggingModule"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.loggingConfig.enabled = false
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        respond {
                            content = "Successfully disabled $loggingModule"
                        }
                    }
                }

                publicSubCommand(::SingleChannelArgs) {
                    name = "set-channel"
                    description = "Set the channel logging embeds will be sent to"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.loggingConfig.channel = arguments.channel.id.value
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        respond {
                            content = "Successfully set Logging channel to ${arguments.channel.mention}"
                        }
                    }
                }
            }
            //endregion

            //region Moderation Module
            group("moderation") {
                description = "Alter the settings of $moderationModule"

                publicSubCommand {
                    name = "enable"
                    description = "Enable $moderationModule"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.moderationConfig.enabled = true
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        respond {
                            content = "Successfully enabled $moderationModule"
                        }
                    }
                }

                publicSubCommand {
                    name = "disable"
                    description = "Disable $moderationModule"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.moderationConfig.enabled = false
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        respond {
                            content = "Successfully disabled $moderationModule"
                        }
                    }
                }

                publicSubCommand(::SingleRoleArgs) {
                    name = "set-role"
                    description = "Set role required to run moderator level commands"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.moderationConfig.moderatorRole = arguments.role.id.value
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        respond {
                            content = "Successfully set moderator role to ${arguments.role.mention}"
                        }
                    }
                }

                publicSubCommand(::SingleBooleanArgs) {
                    name = "log-actions"
                    description = "Enable logging for actions by moderators"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.moderationConfig.enabled = arguments.boolean
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        respond {
                            content =
                                if (arguments.boolean) "Successfully enabled logging for moderation actions" else "Successfully disabled logging for moderation actions"
                        }
                    }
                }
            }
            //endregion
        }
        //endregion
    }

    //region Arguments
    inner class SingleChannelArgs : Arguments() {
        val channel by channel(
            "channel",
            "The channel"
        )
    }

    inner class SingleRoleArgs : Arguments() {
        val role by role(
            "role",
            "The role"
        )
    }

    inner class SingleBooleanArgs : Arguments() {
        val boolean by boolean(
            "enabled",
            "Whether to enable this option"
        )
    }
    //endregion
}
