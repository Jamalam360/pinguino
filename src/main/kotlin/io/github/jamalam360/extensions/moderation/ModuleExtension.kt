package io.github.jamalam360.extensions.moderation

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashGroup
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.UserBehavior
import io.github.jamalam360.database.entity.ServerConfig
import io.github.jamalam360.util.*

/**
 * @author  Jamalam360
 */

@OptIn(KordPreview::class)
class ModuleExtension : Extension() {
    override val name: String = "modules"

    private val quotesModule: String = "the Quotes module"
    private val loggingModule: String = "the Logging module"
    private val moderationModule: String = "the Moderation module"
    private val notificationsModule: String = "the Greetings module"
    private val filePasteModule: String = "the File Paste module"

    private suspend fun SlashGroup.moduleEnable(moduleName: String, lambda: (ServerConfig) -> Unit) {
        ephemeralSubCommand {
            name = "enable"
            description = "Enables the $moduleName module"

            action {
                val config = database.config.getConfig(guild!!.id)
                lambda(config)
                database.config.updateConfig(guild!!.id, config)

                logModuleEnabled(moduleName, user, guild!!)

                respond {
                    content = "Successfully enabled the $moduleName module"
                }
            }
        }
    }

    private suspend fun SlashGroup.moduleDisable(moduleName: String, lambda: (ServerConfig) -> Unit) {
        ephemeralSubCommand {
            name = "disable"
            description = "Disables the $moduleName module"

            action {
                val config = database.config.getConfig(guild!!.id)
                lambda(config)
                database.config.updateConfig(guild!!.id, config)

                logModuleDisabled(moduleName, user, guild!!)

                respond {
                    content = "Successfully disabled the $moduleName module"
                }
            }
        }
    }

    @Suppress("DuplicatedCode")
    override suspend fun setup() {
        //region Slash Commands
        ephemeralSlashCommand {
            name = "module"
            description = "Alter the settings of a specific module"

            check {
                hasModeratorRole()
            }

            group("quotes") {
                description = "Alter the settings of $quotesModule"

                moduleEnable("Quotes") { conf ->
                    conf.quotesConfig.enabled = true
                }

                moduleDisable("Quotes") { conf ->
                    conf.quotesConfig.enabled = false
                }

                ephemeralSubCommand(::SingleChannelArgs) {
                    name = "set-channel"
                    description = "Set the channel quote embeds will be sent to"

                    action {
                        val conf = database.config.getConfig(guild!!.id)
                        conf.quotesConfig.channel = arguments.channel.id.value.toLong()
                        database.config.updateConfig(guild!!.id, conf)

                        log("Quotes Channel Updated", "Channel updated to ${arguments.channel.mention}", user, guild!!)

                        respond {
                            content = "Successfully set Quotes channel to ${arguments.channel.mention}"
                        }
                    }
                }
            }

            group("logging") {
                description = "Alter the settings of $loggingModule"

                moduleEnable("Logging") { conf ->
                    conf.loggingConfig.enabled = true
                }

                moduleDisable("Logging") { conf ->
                    conf.loggingConfig.enabled = false
                }

                ephemeralSubCommand(::SingleChannelArgs) {
                    name = "set-channel"
                    description = "Set the channel logging embeds will be sent to"

                    action {
                        val conf = database.config.getConfig(guild!!.id)
                        conf.loggingConfig.channel = arguments.channel.id.value.toLong()
                        database.config.updateConfig(guild!!.id, conf)

                        log("Logging Channel Updated", "Channel updated to ${arguments.channel.mention}", user, guild!!)

                        respond {
                            content = "Successfully set Logging channel to ${arguments.channel.mention}"
                        }
                    }
                }
            }

            group("moderation") {
                description = "Alter the settings of $moderationModule"

                moduleEnable("Moderation") { conf ->
                    conf.moderationConfig.enabled = true
                }

                moduleDisable("Moderation") { conf ->
                    conf.moderationConfig.enabled = false
                }

                ephemeralSubCommand(::SingleRoleArgs) {
                    name = "set-role"
                    description = "Set role required to run moderator level commands"

                    action {
                        val conf = database.config.getConfig(guild!!.id)
                        conf.moderationConfig.moderatorRole = arguments.role.id.value.toLong()
                        database.config.updateConfig(guild!!.id, conf)

                        log("Moderator Role Updated", "Role updated to ${arguments.role.mention}", user, guild!!)

                        respond {
                            content = "Successfully set moderator role to ${arguments.role.mention}"
                        }
                    }
                }

                ephemeralSubCommand(::SingleRoleArgs) {
                    name = "set-muted-role"
                    description = "Set role to apply to muted users"

                    action {
                        val conf = database.config.getConfig(guild!!.id)
                        conf.moderationConfig.mutedRole = arguments.role.id.value.toLong()
                        database.config.updateConfig(guild!!.id, conf)

                        log("Muted Role Updated", "Role updated to ${arguments.role.mention}", user, guild!!)

                        respond {
                            content = "Successfully set muted role to ${arguments.role.mention}"
                        }
                    }
                }

                ephemeralSubCommand(::SingleBooleanArgs) {
                    name = "auto-save-threads"
                    description = "Set whether threads are prevented from archiving by default"

                    action {
                        val conf = database.config.getConfig(guild!!.id)
                        conf.moderationConfig.autoSaveThreads = arguments.boolean
                        database.config.updateConfig(guild!!.id, conf)

                        log("Auto-save threads updated", "Auto-save threads set to ${arguments.boolean}", user, guild!!)

                        respond {
                            content = "Successfully set auto-save threads to ${arguments.boolean}"
                        }
                    }
                }
            }

            group("greetings") {
                description = "Alter the settings of $notificationsModule"

                moduleEnable("Notifications") { conf ->
                    conf.notificationsConfig.enabled = true
                }

                moduleDisable("Notifications") { conf ->
                    conf.notificationsConfig.enabled = false
                }

                ephemeralSubCommand(::SingleChannelArgs) {
                    name = "set-channel"
                    description = "Set the channel to send greetings and farewells to"

                    action {
                        val conf = database.config.getConfig(guild!!.id)
                        conf.notificationsConfig.greetingChannel = arguments.channel.id.value.toLong()
                        database.config.updateConfig(guild!!.id, conf)

                        log(
                            "Greetings Channel Updated",
                            "Channel updated to ${arguments.channel.mention}",
                            user,
                            guild!!
                        )

                        respond {
                            content = "Successfully set channel to ${arguments.channel.mention}"
                        }
                    }
                }

                ephemeralSubCommand(::GreetingArgs) {
                    name = "set-greeting"
                    description = "Set the message to send to the greeting channel when a member joins"

                    action {
                        val conf = database.config.getConfig(guild!!.id)
                        conf.notificationsConfig.greetingMessage = arguments.string
                        database.config.updateConfig(guild!!.id, conf)

                        log("Greeting Message Updated", "Updated to ${arguments.string}", user, guild!!)

                        respond {
                            content = "Successfully set message to ${arguments.string}"
                        }
                    }
                }

                ephemeralSubCommand(::GreetingArgs) {
                    name = "set-farewell"
                    description = "Set the message to send to the greeting channel when a member leaves"

                    action {
                        val conf = database.config.getConfig(guild!!.id)
                        conf.notificationsConfig.farewellMessage = arguments.string
                        database.config.updateConfig(guild!!.id, conf)

                        log("Farewell Message Updated", "Updated to ${arguments.string}", user, guild!!)

                        respond {
                            content = "Successfully set message to ${arguments.string}"
                        }
                    }
                }
            }

            group("file-paste") {
                description = "Alter the settings of $filePasteModule"

                moduleEnable("File Paste") { conf ->
                    conf.filePasteConfig.enabled = true
                }

                moduleDisable("File Paste") { conf ->
                    conf.filePasteConfig.enabled = false
                }

                ephemeralSubCommand(::HastebinUrlArgs) {
                    name = "set-url"
                    description = "Set the URL to use for the Hastebin API"

                    action {
                        val url = if (arguments.url.endsWith("/")) arguments.url else "${arguments.url}/"

                        val conf = database.config.getConfig(guild!!.id)
                        conf.filePasteConfig.hastebinUrl = url
                        database.config.updateConfig(guild!!.id, conf)

                        log("Hastebin URL Updated", "Updated to ${arguments.url}", user, guild!!)

                        respond {
                            content = "Successfully set Hastebin URL to ${arguments.url}"
                        }
                    }
                }
            }
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
        bot.getLoggingExtension().logAction(
            action,
            extraContent,
            responsibleMod.asUser(),
            guild.asGuild()
        )
    }

    class GreetingArgs : Arguments() {
        val string by string(
            "value",
            "The value - use \$user to use the username of the user in your message"
        )
    }

    class HastebinUrlArgs : Arguments() {
        val url by string(
            "url",
            "The Hastebin server to use for the file paste module. Defaults to the official Hastebin site"
        )
    }
}
