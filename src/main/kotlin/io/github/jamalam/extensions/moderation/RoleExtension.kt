package io.github.jamalam.extensions.moderation

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.suggestStringMap
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam.Modules
import io.github.jamalam.util.*

class RoleExtension : Extension() {
    override val name = "phishing"

    override suspend fun setup() {
        ephemeralSlashCommand {
            name = "role"
            description = "Add or remove available roles from yourself"

            ephemeralSubCommand(::RoleArgs) {
                name = "add"
                description = "Add a role to yourself"

                check {
                    notInDm()
                    isModuleEnabled(Modules.Role)
                }

                action {
                    val roleIdLong = guild!!.getConfig().roleConfig.roles[arguments.name]

                    if (roleIdLong == null) {
                        respond {
                            embed {
                                info("I can't find that role")
                                error()
                                pinguino()
                                now()
                            }
                        }

                        return@action
                    }

                    val roleId = Snowflake(roleIdLong)
                    val role = guild!!.getRoleOrNull(roleId)

                    if (role == null) {
                        respond {
                            embed {
                                info("I can't find that role")
                                error()
                                pinguino()
                                now()
                            }
                        }

                        return@action
                    }

                    guild!!.getMember(user.id).addRole(roleId, "${user.asUser().username} used `/role add`")

                    respond {
                        embed {
                            info("Successfully added that role")
                            success()
                            pinguino()
                            now()
                        }
                    }
                }
            }

            ephemeralSubCommand(::RoleArgs) {
                name = "remove"
                description = "Remove a role from yourself"

                check {
                    notInDm()
                    isModuleEnabled(Modules.Role)
                }

                action {
                    val roleIdLong = guild!!.getConfig().roleConfig.roles[arguments.name]

                    if (roleIdLong == null) {
                        respond {
                            embed {
                                info("I can't find that role")
                                error()
                                pinguino()
                                now()
                            }
                        }

                        return@action
                    }

                    val roleId = Snowflake(roleIdLong)
                    val role = guild!!.getRoleOrNull(roleId)

                    if (role == null) {
                        respond {
                            embed {
                                info("I can't find that role")
                                error()
                                pinguino()
                                now()
                            }
                        }

                        return@action
                    }

                    guild!!.getMember(user.id).removeRole(roleId, "${user.asUser().username} used `/role remove`")

                    respond {
                        embed {
                            info("Successfully removed that role")
                            success()
                            pinguino()
                            now()
                        }
                    }
                }
            }
        }
    }

    inner class RoleArgs : Arguments() {
        val name by string {
            name = "name"
            description = "The name of the role to remove"

            autoComplete {
                if (data.guildId.value != null) {
                    val conf = database.serverConfig.getConfig(data.guildId.value!!)
                    val map = mutableMapOf<String, String>()

                    conf.roleConfig.roles.forEach {
                        map[it.key] = it.key
                    }

                    suggestStringMap(map)
                }
            }
        }
    }
}