package io.github.jamalam360.extensions

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.enumChoice
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
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

    override suspend fun setup() {
        //region Slash Commands

        publicSlashCommand {
            name = "module"
            description = "Alter the settings of bot modules"

            publicSubCommand(::ModuleToggleArgs) {
                name = "enable"
                description = "Enable a module"

                check {
                    hasPermission(Permission.Administrator)
                }

                action {
                    val conf = DATABASE.config.getConfig(guild!!.id)

                    when (arguments.module) {
                        Module.QUOTES -> conf.quotesEnabled = true
                    }

                    DATABASE.config.updateConfig(guild!!.id, conf)

                    respond {
                        content = "Successfully enabled module '${arguments.module.readableName}'"
                    }
                }
            }

            publicSubCommand(::ModuleToggleArgs) {
                name = "disable"
                description = "Disable a module"

                check {
                    hasPermission(Permission.ManageGuild)
                }

                action {
                    val conf = DATABASE.config.getConfig(guild!!.id)

                    when (arguments.module) {
                        Module.QUOTES -> conf.quotesEnabled = false
                    }

                    DATABASE.config.updateConfig(guild!!.id, conf)

                    respond {
                        content = "Successfully disabled module '${arguments.module.readableName}'"
                    }
                }
            }
        }

        //endregion
    }

    //region Arguments

    inner class ModuleToggleArgs : Arguments() {
        val module by enumChoice<Module>(
            "module",
            "the module to enable/disable",
            "module"
        )
    }

    //endregion
}

enum class Module : ChoiceEnum {
    QUOTES {
        override val readableName: String = "quotes"
    }
}
