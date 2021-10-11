package io.github.jamalam360.extensions

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Permission
import io.github.jamalam360.DATABASE

/**
 * @author  Jamalam360
 */

@OptIn(KordPreview::class)
class ModuleExtension : Extension() {
    override val name: String = "modules"

    //region Module Names

    private val quotesModule: String = "the Quotes module"
    private val loggingModule: String = "the Logging module"

    //endregion

    @Suppress("DuplicatedCode")
    override suspend fun setup() {
        //region Slash Commands

        publicSlashCommand {
            name = "module"
            description = "Alter the settings of a specific module"

            //region Quotes Module

            group("quotes") {
                description = "Alter the settings of $quotesModule"

                check {
                    hasPermission(Permission.Administrator)
                }

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
            }

            //endregion

            //region Logging Module

            group("logging") {
                description = "Alter the settings of $loggingModule"

                check {
                    hasPermission(Permission.Administrator)
                }

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
        }

        //endregion
    }

    //region Arguments

    inner class SingleChannelArgs : Arguments() {
        val channel by channel(
            "channel",
            description = "The channel"
        )
    }

    //endregion
}
