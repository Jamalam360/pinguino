package io.github.jamalam360.extensions.bot

import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.commands.events.CommandFailedWithExceptionEvent
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.channel.TextChannel
import io.github.jamalam360.*
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.request.get
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.forEach
import kotlinx.datetime.DateTimePeriod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlin.random.Random
import kotlin.time.ExperimentalTime

typealias GitHubRelease = JsonArray<GitHubReleaseElement>

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

        scheduler.schedule(30) {
            if (DATABASE.botMeta.get().lastVersionUpdateLogPosted != VERSION) {
                kord.guilds.collect {
                    val conf = DATABASE.config.getConfig(it.id)
                    if (conf.pinguinoAnnouncementsConfig.enabled) {
                        val channel = it.getChannelOrNull(Snowflake(conf.pinguinoAnnouncementsConfig.channel))
                        if (channel is TextChannel) {
                            val release = client.get<GitHubRelease>("https://api.github.com/repos/JamCoreDiscord/Pinguino/releases")

                            channel.createEmbed {

                            }
                        }
                    }
                }
            }
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

@Serializable
data class GitHubReleaseElement (
    val url: String,

    @SerialName("html_url")
    val htmlURL: String,

    @SerialName("assets_url")
    val assetsURL: String,

    @SerialName("upload_url")
    val uploadURL: String,

    @SerialName("tarball_url")
    val tarballURL: String,

    @SerialName("zipball_url")
    val zipballURL: String,

    val id: Long,

    @SerialName("node_id")
    val nodeID: String,

    @SerialName("tag_name")
    val tagName: String,

    @SerialName("target_commitish")
    val targetCommitish: String,

    val name: String,
    val body: String,
    val draft: Boolean,
    val prerelease: Boolean,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("published_at")
    val publishedAt: String,

    val author: Author,
    val assets: List<Asset>
)

@Serializable
data class Asset (
    val url: String,

    @SerialName("browser_download_url")
    val browserDownloadURL: String,

    val id: Long,

    @SerialName("node_id")
    val nodeID: String,

    val name: String,
    val label: String,
    val state: String,

    @SerialName("content_type")
    val contentType: String,

    val size: Long,

    @SerialName("download_count")
    val downloadCount: Long,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String,

    val uploader: Author
)

@Serializable
data class Author (
    val login: String,
    val id: Long,

    @SerialName("node_id")
    val nodeID: String,

    @SerialName("avatar_url")
    val avatarURL: String,

    @SerialName("gravatar_id")
    val gravatarID: String,

    val url: String,

    @SerialName("html_url")
    val htmlURL: String,

    @SerialName("followers_url")
    val followersURL: String,

    @SerialName("following_url")
    val followingURL: String,

    @SerialName("gists_url")
    val gistsURL: String,

    @SerialName("starred_url")
    val starredURL: String,

    @SerialName("subscriptions_url")
    val subscriptionsURL: String,

    @SerialName("organizations_url")
    val organizationsURL: String,

    @SerialName("repos_url")
    val reposURL: String,

    @SerialName("events_url")
    val eventsURL: String,

    @SerialName("received_events_url")
    val receivedEventsURL: String,

    val type: String,

    @SerialName("site_admin")
    val siteAdmin: Boolean
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
