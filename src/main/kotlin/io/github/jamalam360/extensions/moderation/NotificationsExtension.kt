package io.github.jamalam360.extensions.moderation

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.core.event.guild.MemberLeaveEvent
import io.github.jamalam360.Modules
import io.github.jamalam360.util.database
import io.github.jamalam360.util.isModuleEnabled

/**
 * @author  Jamalam360
 */

@OptIn(KordPreview::class)
class NotificationsExtension : Extension() {
    override val name = "notifications"

    override suspend fun setup() {
        //region Events
        event<MemberJoinEvent> {
            check {
                isModuleEnabled(Modules.Notifications)
            }

            action {
                sendGreeting(event.guild.asGuild(), event.member.asUser())
            }
        }

        event<MemberLeaveEvent> {
            check {
                isModuleEnabled(Modules.Notifications)
            }

            action {
                sendFarewell(event.guild.asGuild(), event.user)
            }
        }
        //endregion
    }

    //region Util Methods
    private suspend fun sendGreeting(guild: Guild, user: User) {
        val conf = database.config.getConfig(guild.id)

        if (conf.notificationsConfig.greetingChannel != null) {
            val channel = guild.getChannel(Snowflake(conf.notificationsConfig.greetingChannel!!))

            if (channel.type == ChannelType.GuildText) {
                (channel as MessageChannel).createEmbed {
                    title = if (conf.notificationsConfig.greetingMessage == null) {
                        "Everybody welcome ${user.username} to ${guild.name}!"
                    } else {
                        conf.notificationsConfig.greetingMessage!!.replace("\$user", user.username)
                    }
                    image = user.avatar!!.url
                }
            }
        }
    }

    private suspend fun sendFarewell(guild: Guild, user: User) {
        val conf = database.config.getConfig(guild.id)

        if (conf.notificationsConfig.greetingChannel != null) {
            val channel = guild.getChannel(Snowflake(conf.notificationsConfig.greetingChannel!!))

            if (channel.type == ChannelType.GuildText) {
                (channel as MessageChannel).createEmbed {
                    title = if (conf.notificationsConfig.farewellMessage == null) {
                        "Everybody say goodbye to ${user.username}"
                    } else {
                        conf.notificationsConfig.farewellMessage!!.replace("\$user", user.username)
                    }
                    image = user.avatar!!.url
                }
            }
        }
    }
    //endregion
}
