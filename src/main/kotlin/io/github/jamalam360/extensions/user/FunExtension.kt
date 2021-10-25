package io.github.jamalam360.extensions.user

import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.channel.withTyping
import dev.kord.core.behavior.interaction.edit
import dev.kord.rest.builder.message.EmbedBuilder
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * @author  Jamalam360
 */

@OptIn(KordPreview::class, ExperimentalTime::class)
class FunExtension : Extension() {
    override val name: String = "fun"
    private val random = Random.Default
    private val client = HttpClient {
        install(JsonFeature)
    }

    override suspend fun setup() {
        publicSlashCommand {
            name = "fun"
            description = "A collection of fun commands!"

            publicSubCommand {
                name = "dice"
                description = "Roll a dice!"

                action {
                    respond {
                        content = "${user.mention} is rolling a dice!"
                    }

                    channel.withTyping {
                        delay(Duration.seconds(3))
                    }

                    interactionResponse.edit {
                        content = "${user.mention} rolled a ${random.nextInt(1, 7)}!"
                    }
                }
            }

            publicSubCommand {
                name = "coin"
                description = "Flip a coin!"

                action {
                    respond {
                        content = "${user.mention} is flipping a coin!"
                    }

                    channel.withTyping {
                        delay(Duration.seconds(3))
                    }

                    interactionResponse.edit {
                        content = if (random.nextBoolean()) {
                            "It's heads!"
                        } else {
                            "It's tails!"
                        }
                    }
                }
            }

            publicSubCommand {
                name = "dog"
                description = "Get a random photo of a dog!"

                action {
                    val response = client.get<DogApiResponse>("https://dog.ceo/api/breeds/image/random")

                    if (response.status == "success") {
                        respond {
                            val embed = EmbedBuilder()
                            embed.image = response.message
                            embed.title = "Look at this cute dog!"
                            embeds.add(embed)
                        }
                    } else {
                        respond {
                            content = "An unexpected error occurred"
                        }
                    }
                }
            }
        }
    }
}

@Serializable
data class DogApiResponse(val message: String, val status: String)
