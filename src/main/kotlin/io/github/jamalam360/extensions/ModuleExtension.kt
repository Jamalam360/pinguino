package io.github.jamalam360.extensions

import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.UserBehavior
import io.github.jamalam360.*

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
        ephemeralSlashCommand {
            name = "module"
            description = "Alter the settings of a specific module"

            check {
                hasModeratorRole()
            }

            //region Quotes Module
            group("quotes") {
                description = "Alter the settings of $quotesModule"

                ephemeralSubCommand {
                    name = "enable"
                    description = "Enable $quotesModule"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.quotesConfig.enabled = true
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        logModuleEnabled(Modules.Quotes.readableName, user, guild!!)

                        respond {
                            content = "Successfully enabled $quotesModule"
                        }
                    }
                }

                ephemeralSubCommand {
                    name = "disable"
                    description = "Disable $quotesModule"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.quotesConfig.enabled = false
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        logModuleDisabled(Modules.Quotes.readableName, user, guild!!)

                        respond {
                            content = "Successfully disabled $quotesModule"
                        }
                    }
                }

                ephemeralSubCommand(::SingleChannelArgs) {
                    name = "set-channel"
                    description = "Set the channel quote embeds will be sent to"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.quotesConfig.channel = arguments.channel.id.value
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        log("Quotes Channel Updated", "Channel updated to ${arguments.channel.mention}", user, guild!!)

                        respond {
                            content = "Successfully set Quotes channel to ${arguments.channel.mention}"
                        }
                    }
                }

                ephemeralSubCommand(::SingleBooleanArgs) {
                    name = "enable-logging"
                    description = "Whether to log quotes in the moderator log channel"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.quotesConfig.log = arguments.boolean
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        log(if (arguments.boolean) "Quotes Log Enabled" else "Quotes Log Disabled", "", user, guild!!)

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

                ephemeralSubCommand {
                    name = "enable"
                    description = "Enable $loggingModule"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.loggingConfig.enabled = true
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        logModuleEnabled(Modules.Logging.readableName, user, guild!!)

                        respond {
                            content = "Successfully enabled $loggingModule"
                        }
                    }
                }

                ephemeralSubCommand {
                    name = "disable"
                    description = "Disable $loggingModule"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.loggingConfig.enabled = false
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        logModuleDisabled(Modules.Logging.readableName, user, guild!!)

                        respond {
                            content = "Successfully disabled $loggingModule"
                        }
                    }
                }

                ephemeralSubCommand(::SingleChannelArgs) {
                    name = "set-channel"
                    description = "Set the channel logging embeds will be sent to"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.loggingConfig.channel = arguments.channel.id.value
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        log("Logging Channel Updated", "Channel updated to ${arguments.channel.mention}", user, guild!!)

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

                ephemeralSubCommand {
                    name = "enable"
                    description = "Enable $moderationModule"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.moderationConfig.enabled = true
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        logModuleEnabled(Modules.Moderation.readableName, user, guild!!)

                        respond {
                            content = "Successfully enabled $moderationModule"
                        }
                    }
                }

                ephemeralSubCommand {
                    name = "disable"
                    description = "Disable $moderationModule"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.moderationConfig.enabled = false
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        logModuleDisabled(Modules.Moderation.readableName, user, guild!!)

                        respond {
                            content = "Successfully disabled $moderationModule"
                        }
                    }
                }

                ephemeralSubCommand(::SingleRoleArgs) {
                    name = "set-role"
                    description = "Set role required to run moderator level commands"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.moderationConfig.moderatorRole = arguments.role.id.value
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        log("Moderator Role Updated", "Role updated to ${arguments.role.mention}", user, guild!!)

                        respond {
                            content = "Successfully set moderator role to ${arguments.role.mention}"
                        }
                    }
                }

                ephemeralSubCommand(::SingleBooleanArgs) {
                    name = "log-actions"
                    description = "Enable logging for actions by moderators"

                    action {
                        val conf = DATABASE.config.getConfig(guild!!.id)
                        conf.moderationConfig.enabled = arguments.boolean
                        DATABASE.config.updateConfig(guild!!.id, conf)

                        log(
                            if (arguments.boolean) "Moderation Log Enabled" else "Moderation Log Disabled",
                            "",
                            user,
                            guild!!
                        )

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

    private suspend fun logModuleEnabled(module: String, responsibleMod: UserBehavior, guild: GuildBehavior) {
        log("Module Enabled", "Module '_${module}_' enabled", responsibleMod.asUser(), guild.asGuild())
    }

    private suspend fun logModuleDisabled(module: String, responsibleMod: UserBehavior, guild: GuildBehavior) {
        log("Module Disabled", "Module '_${module}_' disabled", responsibleMod.asUser(), guild.asGuild())
    }

    private suspend fun log(action: String, extraContent: String, responsibleMod: UserBehavior, guild: GuildBehavior) {
        (bot.extensions["logging"] as LoggingExtension).logAction(
            action,
            extraContent,
            responsibleMod.asUser(),
            guild.asGuild()
        )
    }
}
