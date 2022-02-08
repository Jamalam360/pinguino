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

import ch.qos.logback.core.joran.spi.ConsoleTarget

def production = System.getenv().getOrDefault("PRODUCTION", "false")
def logFile = System.getenv().getOrDefault("LOG_FILE", "pinguino.log")
def defaultLevelString = System.getenv().getOrDefault("LOG_LEVEL", "INFO")

def defaultLevel = INFO

switch (defaultLevelString) {
    case "INFO":
        defaultLevel = INFO
        break
    case "DEBUG":
        defaultLevel = DEBUG
        break
    case "TRACE":
        defaultLevel = TRACE
}

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss:SSS Z} | %5level | %40.40logger{40} | %msg%n"
    }

    target = ConsoleTarget.SystemErr
}

appender("FILE", FileAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss:SSS Z} | %5level | %40.40logger{40} | %msg%n"
    }

    file = logFile
    append = production == "true"
    immediateFlush = true
}

root(defaultLevel, ["CONSOLE", "FILE"])
