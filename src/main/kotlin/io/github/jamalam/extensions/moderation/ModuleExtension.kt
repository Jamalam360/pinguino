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

package io.github.jamalam.extensions.moderation

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashGroup
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.commands.converters.impl.enum
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam.Modules
import io.github.jamalam.database.entity.ServerConfig
import io.github.jamalam.database.entity.ServerPhishingModerationType
import io.github.jamalam.util.*

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
    private val phishingModule: String = "the Phishing module"

    private suspend fun SlashGroup.moduleEnable(moduleName: String, lambda: (ServerConfig) -> Unit) {
        ephemeralSubCommand {
            name = "enable"
            description = "Enables the $moduleName module"

            action {
                val config = guild!!.getConfig()
                lambda(config)
                database.config.updateConfig(guild!!.id, config)

                logModuleEnabled(moduleName, user, guild!!)

                respond {
                    embed {
                        info("Enabled $moduleName module")
                        pinguino()
                        now()
                        success()
                    }
                }
            }
        }
    }

    private suspend fun SlashGroup.moduleDisable(moduleName: String, lambda: (ServerConfig) -> Unit) {
        ephemeralSubCommand {
            name = "disable"
            description = "Disables the $moduleName module"

            action {
                val config = guild!!.getConfig()
                lambda(config)
                database.config.updateConfig(guild!!.id, config)

                logModuleDisabled(moduleName, user, guild!!)

                respond {
                    embed {
                        info("Disabled $moduleName module")
                        pinguino()
                        now()
                        success()
                    }
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
                        val conf = guild!!.getConfig()
                        conf.quotesConfig.channel = arguments.channel.id.value.toLong()
                        database.config.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Quotes channel updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            channelField("Channel", arguments.channel)
                        }

                        respond {
                            embed {
                                info("Quotes channel updated")
                                pinguino()
                                now()
                                success()
                            }
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
                        val conf = guild!!.getConfig()
                        conf.loggingConfig.channel = arguments.channel.id.value.toLong()
                        database.config.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Logging channel updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            channelField("Channel", arguments.channel)
                        }

                        respond {
                            embed {
                                info("Logging channel updated")
                                pinguino()
                                now()
                                success()
                            }
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
                        val conf = guild!!.getConfig()
                        conf.moderationConfig.moderatorRole = arguments.role.id.value.toLong()
                        database.config.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Moderator role updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            stringField("Role", arguments.role.mention)
                        }

                        respond {
                            embed {
                                info("Moderator role updated")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    }
                }

                ephemeralSubCommand(::SingleBooleanArgs) {
                    name = "auto-save-threads"
                    description = "Set whether threads are prevented from archiving by default"

                    action {
                        val conf = guild!!.getConfig()
                        conf.moderationConfig.autoSaveThreads = arguments.boolean
                        database.config.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Auto-save-threads updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            stringField("Value", arguments.boolean.toString())
                        }

                        respond {
                            embed {
                                info("Auto-save-threads updated")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    }
                }

                ephemeralSubCommand(::SingleChannelArgs) {
                    name = "set-public-mod-log-channel"
                    description = "Set the channel public mod-logs will be sent to"

                    action {
                        val conf = guild!!.getConfig()
                        conf.moderationConfig.publicModLogChannel = arguments.channel.id.value.toLong()
                        database.config.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Public mod-log channel updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            channelField("Channel", arguments.channel)
                        }

                        respond {
                            embed {
                                info("Public mod-log channel updated")
                                pinguino()
                                now()
                                success()
                            }
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
                        val conf = guild!!.getConfig()
                        conf.notificationsConfig.greetingChannel = arguments.channel.id.value.toLong()
                        database.config.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Greetings channel updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            channelField("Channel", arguments.channel)
                        }

                        respond {
                            embed {
                                info("Greetings channel updated")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    }
                }

                ephemeralSubCommand(::GreetingArgs) {
                    name = "set-greeting"
                    description = "Set the message to send to the greeting channel when a member joins"

                    action {
                        val conf = guild!!.getConfig()
                        conf.notificationsConfig.greetingMessage = arguments.string
                        database.config.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Greeting message updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            stringField("Message", arguments.string)
                        }

                        respond {
                            embed {
                                info("Greetings message updated")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    }
                }

                ephemeralSubCommand(::GreetingArgs) {
                    name = "set-farewell"
                    description = "Set the message to send to the greeting channel when a member leaves"

                    action {
                        val conf = guild!!.getConfig()
                        conf.notificationsConfig.farewellMessage = arguments.string
                        database.config.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Farewell message updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            stringField("Message", arguments.string)
                        }

                        respond {
                            embed {
                                info("Farewell message updated")
                                pinguino()
                                now()
                                success()
                            }
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

                        val conf = guild!!.getConfig()
                        conf.filePasteConfig.hastebinUrl = url
                        database.config.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Hastebin URL updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            stringField("Message", arguments.url)
                        }

                        respond {
                            embed {
                                info("Hastebin URL updated")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    }
                }
            }

            group("phishing") {
                description = "Alter the settings of $phishingModule"

                moduleEnable("Phishing") { conf ->
                    conf.phishingConfig.enabled = true
                }

                moduleDisable("Phishing") { conf ->
                    conf.phishingConfig.enabled = false
                }

                ephemeralSlashCommand(::PhishingDisciplineLevelArgs) {
                    name = "set-discipline-level"
                    description = "Set the level of discipline to use for posting a phishing link"

                    check {
                        isModuleEnabled(Modules.Phishing)
                        hasModeratorRole()
                    }

                    action {
                        val conf = guild!!.getConfig()
                        conf.phishingConfig.moderationType = arguments.level
                        database.config.updateConfig(guild!!.id, conf)

                        guild!!.getLogChannel()?.createEmbed {
                            info("Phishing discipline level updated")
                            userAuthor(user.asUser())
                            now()
                            log()
                            stringField("Discipline level", arguments.level.readableName)
                        }

                        respond {
                            embed {
                                info("Phishing discipline level updated")
                                pinguino()
                                now()
                                success()
                            }
                        }
                    }
                }
            }
        }
        //endregion
    }

    private suspend fun logModuleEnabled(module: String, responsibleMod: UserBehavior, guild: GuildBehavior) {
        guild.getLogChannel()?.createEmbed {
            info("Module Enabled")
            userAuthor(responsibleMod.asUser())
            now()
            log()
            stringField("Module", module)
        }
    }

    private suspend fun logModuleDisabled(module: String, responsibleMod: UserBehavior, guild: GuildBehavior) {
        guild.getLogChannel()?.createEmbed {
            info("Module Disabled")
            userAuthor(responsibleMod.asUser())
            now()
            log()
            stringField("Module", module)
        }
    }

    class GreetingArgs : Arguments() {
        val string by string {
            name = "value"
            description = "The value - use \$user to use the username of the user in your message"
        }
    }

    class HastebinUrlArgs : Arguments() {
        val url by string {
            name = "url"
            description = "The Hastebin server to use for the file paste module. Defaults to the official Hastebin site"
        }
    }

    class PhishingDisciplineLevelArgs : Arguments() {
        val level by enum<ServerPhishingModerationType> {
            name = "level"
            description = "The level of discipline to use for posting a phishing link"
            typeName = "level"
        }
    }
}
