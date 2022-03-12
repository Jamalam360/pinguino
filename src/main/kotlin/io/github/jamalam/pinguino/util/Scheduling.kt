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

package io.github.jamalam.pinguino.util

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import com.kotlindiscord.kord.extensions.utils.dm
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.editRolePermission
import dev.kord.core.behavior.channel.threads.edit
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam.pinguino.database.entity.ScheduledTask
import io.github.jamalam.pinguino.database.entity.ScheduledTaskType
import io.github.jamalam.pinguino.extensions.moderation.speakingPermissions
import kotlinx.datetime.toDateTimePeriod
import kotlinx.datetime.toKotlinInstant
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

private val tasksToRemove = mutableListOf<ScheduledTask>()

@OptIn(ExperimentalTime::class)
fun scheduleScheduledDatabaseCheck(bot: ExtensibleBot) {
    scheduler.schedule(1) {
        val currentDate = Date()
        database.scheduledTasks.getTasks().forEach {
            val taskDate = Date(it.startTime + it.duration * 1000)
            when (it.type) {
                ScheduledTaskType.PostMessageToChannel -> {
                    if (currentDate.after(taskDate)) {
                        val channel =
                            bot.getKoin().get<Kord>().getChannelOf<MessageChannel>(Snowflake(it.data["channel"]!!))
                        channel?.createMessage(it.data["message"]!!)
                        tasksToRemove.add(it)
                    }
                }
                ScheduledTaskType.PostEmbedToChannel -> {
                    if (currentDate.after(taskDate)) {
                        val channel =
                            bot.getKoin().get<Kord>().getChannelOf<MessageChannel>(Snowflake(it.data["channel"]!!))
                        channel?.createEmbed {
                            title = it.data["title"]!!
                            description = it.data["description"]!!
                            image = it.data["image"]!!

                            if (it.data["author"]!!.toLong() != -1L) {
                                val author = bot.getKoin().get<Kord>().getUser(Snowflake(it.data["author"]!!))
                                author {
                                    name = author?.username
                                    icon = author?.avatar?.url
                                }
                            }
                        }

                        tasksToRemove.add(it)
                    }
                }
                ScheduledTaskType.PostUnmutedLogs -> {
                    if (currentDate.after(taskDate)) {
                        val guild =
                            bot.getKoin().get<Kord>().getGuild(Snowflake(it.data["guild"]!!))

                        if (guild != null) {
                            val member = guild.getMemberOrNull(Snowflake(it.data["member"]!!))
                            val moderator = guild.getMemberOrNull(Snowflake(it.data["moderator"]!!))

                            if (member != null && moderator != null) {
                                if (!member.isBot) {
                                    member.dm {
                                        embed {
                                            info("Unmuted in ${guild.asGuild().name}!")
                                            userAuthor(moderator.asUser())
                                            now()
                                            success()
                                        }
                                    }
                                }

                                guild.getLogChannel()?.createEmbed {
                                    info("Member Unmuted")
                                    userAuthor(moderator.asUser())
                                    log()
                                    userField("Member", member.asUser())
                                }

                                guild.getPublicModLogChannel()?.createEmbed {
                                    info("Member Unmuted")
                                    userAuthor(moderator.asUser())
                                    log()
                                    userField("Member", member.asUser())
                                }
                            }
                        }

                        tasksToRemove.add(it)
                    }
                }
                ScheduledTaskType.PostChannelUnlockedLogs -> {
                    if (currentDate.after(taskDate)) {
                        if (it.data["type"]!! == "thread") {
                            val channel =
                                bot.getKoin().get<Kord>().getChannelOf<ThreadChannel>(Snowflake(it.data["channel"]!!))

                            if (channel != null) {
                                val guild = channel.guild
                                val moderator = guild.getMemberOrNull(Snowflake(it.data["moderator"]!!))?.asUser()

                                if (moderator != null) {
                                    channel.edit {
                                        locked = false
                                        reason = "Unlocking channel after timeout set by ${moderator.username}"
                                    }

                                    channel.createEmbed {
                                        info("Thread unlocked")
                                        userAuthor(moderator.asUser())
                                        now()
                                        success()
                                    }

                                    guild.getLogChannel()?.createEmbed {
                                        info("Thread unlocked automatically after timeout")
                                        userAuthor(moderator.asUser())
                                        now()
                                        log()
                                        channelField("Channel", channel.asChannel())
                                    }
                                }
                            }
                        } else {
                            val channel =
                                bot.getKoin().get<Kord>()
                                    .getChannelOf<GuildMessageChannel>(Snowflake(it.data["channel"]!!))

                            if (channel != null) {
                                val guild = channel.guild
                                val moderator = guild.getMemberOrNull(Snowflake(it.data["moderator"]!!))?.asUser()

                                if (moderator != null) {
                                    (channel as TextChannel).editRolePermission(guild.id) {
                                        speakingPermissions.forEach { perm ->
                                            allowed += perm
                                        }

                                        reason = "Unlocking channel after timeout set by ${moderator.username}"
                                    }

                                    channel.createEmbed {
                                        info("Channel unlocked")
                                        userAuthor(moderator.asUser())
                                        now()
                                        success()
                                    }

                                    guild.getLogChannel()?.createEmbed {
                                        info("Channel unlocked automatically after timeout")
                                        userAuthor(moderator.asUser())
                                        now()
                                        log()
                                        channelField("Channel", channel.asChannel())
                                    }

                                    guild.getPublicModLogChannel()?.createEmbed {
                                        info("Channel unlocked")
                                        userAuthor(moderator.asUser())
                                        now()
                                        log()
                                        channelField("Channel", channel.asChannel())
                                    }
                                }
                            }
                        }

                        tasksToRemove.add(it)
                    }
                }
                ScheduledTaskType.SendReminder -> {
                    if (currentDate.after(taskDate)) {
                        val user =
                            bot.getKoin().get<Kord>().getUser(Snowflake(it.data["user"]!!))

                        val diff =
                            currentDate.toInstant().toKotlinInstant().minus(taskDate.toInstant().toKotlinInstant())

                        println(diff.toDateTimePeriod().toPrettyString())
                        println(diff.toDouble(DurationUnit.SECONDS))

                        val footer = if (diff.toDouble(DurationUnit.SECONDS) > 10) {
                            "This reminder was delivered ${diff.toDateTimePeriod().toPrettyString()} late"
                        } else {
                            "This reminder was delivered on-time"
                        }

                        user?.dm {
                            embed {
                                info(
                                    "Reminder set ${
                                        Date(it.startTime).toInstant().toKotlinInstant().toDiscord(
                                            TimestampType.RelativeTime
                                        )
                                    }"
                                )
                                stringField("Message", it.data["message"]!!)
                                pinguino()
                                success()
                                now()

                                footer {
                                    text = footer
                                }
                            }
                        }

                        tasksToRemove.add(it)
                    }
                }
            }
        }

        tasksToRemove.forEach {
            database.scheduledTasks.removeTask(it)
        }

        scheduleScheduledDatabaseCheck(bot)
    }
}
