package io.github.jamalam360.extensions.user

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.channel.withTyping
import dev.kord.core.behavior.interaction.edit
import dev.kord.rest.builder.message.create.embed
import io.github.jamalam360.util.lenientClient
import io.github.jamalam360.api.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URLEncoder
import java.nio.charset.Charset
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
    private val dog = DogApi()
    private val eightBall = EightBallApi()

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
                        respond {
                            embed {
                                title = "Look at this cute dog!"
                                image = dog.getRandomDog()
                            }
                        }
                }
            }

            publicSubCommand(::EightBallArgs) {
                name = "eight-ball"
                description = "Ask the magic 8-ball a question!"

                action {
                    respond {
                        content = "Asking the magic eight ball ${arguments.question}"
                    }

                    channel.withTyping {
                        delay(Duration.seconds(3))
                    }

                    interactionResponse.edit {
                        content = "The magic eight ball answered: ${eightBall.ask(arguments.question)}"
                    }
                }
            }

            publicSubCommand(::XkcdArgs) {
                name = "xkcd"
                description = "Get an xkcd comic!"

                action {
                    val response = lenientClient.get<XkcdApiResponse>(
                        if (arguments.comic != null) {
                            "https://xkcd.com/${arguments.comic}/info.0.json"
                        } else {
                            "https://xkcd.com/info.0.json"
                        }
                    )

                    respond {
                        embed {
                            title = if (arguments.comic != null) {
                                "xkcd #${arguments.comic} - ${response.title}"
                            } else {
                                "Today's xkcd - ${response.title}"
                            }

                            description = response.alt
                            image = response.img
                        }
                    }
                }
            }

            publicSubCommand {
                name = "kanye"
                description = "Kanye as a service (send help)"

                action {
                    val response = lenientClient.get<KanyeApiResponse>("https://api.kanye.rest/")

                    respond {
                        content = "'${response.quote}' - Kanye West"
                    }
                }
            }

            publicSubCommand(::AgeArgs) {
                name = "age"
                description = "Predict the age of a person!"

                action {
                    val response = lenientClient.get<AgeApiResponse>(
                        "https://api.agify.io/?name=${
                            URLEncoder.encode(
                                arguments.name,
                                Charset.defaultCharset()
                            )
                        }"
                    )

                    respond {
                        content = "With a name like ${arguments.name}, they must be ${response.age} years old!"
                    }
                }
            }

            publicSubCommand {
                name = "bored"
                description = "Get a random activity, for when you're bored!"

                action {
                    val response = lenientClient.get<BoredApiResponse>("https://www.boredapi.com/api/activity/")

                    respond {
                        content = response.activity
                    }
                }
            }

            publicSubCommand {
                name = "chuck-norris-joke"
                description = "Get a random Chuck Norris joke!"

                action {
                    val response = lenientClient.get<ChuckNorrisApiResponse>("https://api.chucknorris.io/jokes/random")

                    respond {
                        content = response.value
                    }
                }
            }

            publicSubCommand {
                name = "donald"
                description = "Get a random Donald Trump quote!"

                action {
                    val response = lenientClient.get<DonaldApiResponse>("https://www.tronalddump.io/random/quote")

                    respond {
                        content = "'${response.value}' - Donald Trump"
                    }
                }
            }

            publicSubCommand {
                name = "cat"
                description = "Get a random cat picture!"

                action {
                    val response = lenientClient.get<CatApiResponse>("https://aws.random.cat/meow")

                    respond {
                        embed {
                            title = "Look at this cute cat!"
                            image = response.file
                        }
                    }
                }
            }

            publicSubCommand {
                name = "dad-joke"
                description = "Get a random dad joke!"

                action {
                    val response = lenientClient.get<DadJokeApiResponse>("https://icanhazdadjoke.com/") {
                        contentType(ContentType.Application.Json)
                    }

                    respond {
                        content = response.joke
                    }
                }
            }
        }
    }

    inner class EightBallArgs : Arguments() {
        val question by string(
            "question",
            "The question you want to ask the magic 8-ball",
        )
    }

    inner class XkcdArgs : Arguments() {
        val comic by optionalInt(
            "comic",
            "The comic you want to get, or today's comic if not specified",
        )
    }

    inner class AgeArgs : Arguments() {
        val name by string(
            "name",
            "The name of the person you want to predict the age of",
        )
    }
}

