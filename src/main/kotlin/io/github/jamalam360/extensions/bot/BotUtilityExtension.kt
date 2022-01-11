package io.github.jamalam360.extensions.bot

import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.commands.events.CommandFailedWithExceptionEvent
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.Kord
import io.github.jamalam360.*
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.count
import kotlinx.datetime.DateTimePeriod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random
import kotlin.time.ExperimentalTime

/**
 * @author  Jamalam360
 */
@OptIn(ExperimentalTime::class)
class BotUtilityExtension : Extension() {

    override val name = "utility"
    private val client = HttpClient {
        install(JsonFeature)
    }
    private val scheduler = Scheduler()

    private val presenceDelay = DateTimePeriod(minutes = 2, seconds = 30) //Every 2.5 minutes
    private val dblDelay = DateTimePeriod(minutes = 10) //Every 10 minutes

    override suspend fun setup() {
        // Set initial presence (without the delay there is an error)
        scheduler.schedule(10) {
            this.kord.editPresence {
                status = PresenceStatus.Idle
                playing("booting up!")
            }

            setDBLStats()
        }

        scheduler.schedule(presenceDelay.seconds.toLong()) {
            setPresenceStatus()
        }

        event<CommandFailedWithExceptionEvent<*, *>> {
            action {
                if (PRODUCTION) {
                    val errorString =
                        "Command: `" + event.command.name + "`" + "\n" + "Error: `" + event.throwable.message + "`" + "\n" + "Stacktrace: ```" + event.throwable.stackTrace.joinToString(
                            "\n"
                        ) { "     $it" } + "```"

                    client.post<HttpResponse>(ERROR_WEBHOOK_URL) {
                        contentType(ContentType.Application.Json)
                        body = ErrorWebhookBody(
                            username = "Pinguino",
                            avatarUrl = PINGUINO_PFP,
                            content = "",
                            embeds = listOf(
                                ErrorWebhookEmbed(
                                    description =
                                    errorString.limit(250),
                                    color = DISCORD_RED.rgb,
                                    title = "Command Failed",
                                    author = ErrorWebhookAuthor(
                                        name = "Pinguino",
                                        icon = PINGUINO_PFP
                                    )
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    private fun String.limit(limit: Int): String {
        return if (this.length > limit) {
            this.substring(0, limit - 3) + "..."
        } else {
            this
        }
    }

    private suspend fun setPresenceStatus() {
        BotStatus.random().setPresenceStatus(this.kord)
        scheduler.schedule(seconds = presenceDelay.seconds.toLong()) {
            setPresenceStatus()
        }
    }

    private suspend fun setDBLStats() {
        if (PRODUCTION) {
            client.post<HttpResponse>(DBL_URL) {
                contentType(ContentType.Application.Json)

                headers {
                    append("Authorization", DBL_TOKEN)
                }
                body = DBLStatisticBody(kord.guilds.count())
            }

            scheduler.schedule(seconds = dblDelay.seconds.toLong()) {
                setDBLStats()
            }
        }
    }
}

@Serializable
data class DBLStatisticBody(
    @SerialName("server_count")
    val count: Int
)

@Serializable
data class ErrorWebhookBody(
    val username: String,
    @SerialName("avatar_url")
    val avatarUrl: String,
    val content: String,
    val embeds: List<ErrorWebhookEmbed>
)

@Serializable
data class ErrorWebhookEmbed(
    val author: ErrorWebhookAuthor = ErrorWebhookAuthor(),
    val title: String = "An Error Occurred!",
    val color: Int = DISCORD_RED.rgb,
    val description: String
)

@Serializable
data class ErrorWebhookAuthor(
    val name: String = "Pinguino",
    @SerialName("icon_url")
    val icon: String = PINGUINO_PFP
)

@Suppress("unused")
enum class BotStatus(val setPresenceStatus: suspend (kord: Kord) -> Unit) {
    ServerCount({
        it.editPresence {
            status = PresenceStatus.Online
            watching("over ${it.guilds.count()} servers")
        }
    }),
    CurrentVersion({
        it.editPresence {
            status = PresenceStatus.Online
            playing("Pinguino $VERSION")
        }
    }),
    RandomFun({
        it.editPresence {
            status = PresenceStatus.Online

            val status = RandomStatus.random()

            when (status.type) {
                StatusType.Watching -> watching(status.message)
                StatusType.Playing -> playing(status.message)
                StatusType.Listening -> listening(status.message)
                StatusType.Competing -> competing(status.message)
            }
        }
    });

    companion object {
        private val values = values()
        private val size = values.size

        fun random(): BotStatus = values[Random.nextInt(size)]
    }
}

@Suppress("unused")
enum class RandomStatus(val type: StatusType, val message: String) {
    ListeningForYourCommands(StatusType.Listening, "your commands"),
    WatchingForYourCommands(StatusType.Watching, "for your commands"),
    WatchingTheFootball(StatusType.Watching, "the football"),
    PlayingMinecraft(StatusType.Playing, "Minecraft :D"),
    PlayingABoardGame(StatusType.Playing, "a board game"),
    CompetingInTheOlympics(StatusType.Competing, "the olympics");

    companion object {
        private val values = values()
        private val size = values.size

        fun random(): RandomStatus = values[Random.nextInt(size)]
    }
}

enum class StatusType {
    Watching,
    Playing,
    Listening,
    Competing
}
