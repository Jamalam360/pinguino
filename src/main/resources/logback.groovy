import ch.qos.logback.core.joran.spi.ConsoleTarget

def production = System.getenv().getOrDefault("PRODUCTION", "false")
def defaultLevel = INFO

if (production == "true") {
    defaultLevel = DEBUG

    // Silence warning about missing native PRNG on Windows
    logger("io.ktor.util.random", ERROR)
}

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss:SSS Z} | %5level | %40.40logger{40} | %msg%n"
    }

    target = ConsoleTarget.SystemErr
}

root(defaultLevel, ["CONSOLE"])
