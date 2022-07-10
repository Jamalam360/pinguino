package io.github.jamalam360.pinguino.config.parse

import com.charleskorn.kaml.Yaml
import io.github.jamalam360.pinguino.config.types.root.BotConfig
import io.github.jamalam360.pinguino.config.types.root.CommonConfig
import io.github.jamalam360.pinguino.logging.getLogger
import java.nio.file.Path
import kotlin.system.exitProcess

/**
 * @author  Jamalam
 */

class ConfigLoader {
    private var common: CommonConfig? = null
    private var bot: BotConfig? = null
    private var logger = getLogger("Config", "Loader")

    fun common(): CommonConfig {
        if (common == null) {
            error("Common config is null")
        }

        return common as CommonConfig
    }

    fun bot(): BotConfig {
        if (bot == null) {
            error("Bot config is null")
        }

        return bot as BotConfig
    }

    //TODO(Jamalam360): Gracefully handle missing config files with defaults.
    private fun load(path: String) {
        val commonFile = Path.of(path, "common.yaml").toFile()
        val botFile = Path.of(path, "bot.yaml").toFile()

        if (!commonFile.exists() || !botFile.exists()) {
            logger.warn("One or more required config files are missing, writing defaults to the config directory.")
            logger.warn("The bot will restart to allow you to edit the default config files.")

//            if (!commonFile.exists()) {
//                commonFile.writeText()
//            }
//
//            if (!botFile.exists()) {
//                botFile.writeText(Yaml.default.encodeToString(BotConfig.default))
//            }

            exitProcess(1)
        }

        common = Yaml.default.decodeFromString(
            CommonConfig.serializer(),
            interpolateEnvironmentVariables(commonFile.readText())
        )

        bot = Yaml.default.decodeFromString(
            BotConfig.serializer(),
            interpolateEnvironmentVariables(botFile.readText())
        )

        logger.info("Common config loaded from ${commonFile.path}")
        logger.info("Bot config loaded from ${botFile.path}")
    }

    private fun interpolateEnvironmentVariables(text: String): String {
        var result = text

        for (variable in Regex("\\\$([\\s\\S].+)").findAll(text)) {
            val (key) = variable.groupValues
            val value =
                System.getenv(key) ?: error("Environment variable $key is not set, but was referenced in config")
            result = result.replace("\$$key", value)
        }

        return result
    }

    companion object {
        fun load(path: String): ConfigLoader {
            val loader = ConfigLoader()
            loader.load(path)
            return loader
        }
    }
}
