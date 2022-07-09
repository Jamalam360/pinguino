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

//TODO(Jamalam360): Read from the configuration file and set logging settings, somehow.

import ch.qos.logback.core.joran.spi.ConsoleTarget
import java.text.SimpleDateFormat
import groovy.io.FileType

def defaultLevelString = System.getenv().getOrDefault("LOG_LEVEL", "INFO")
def defaultLevel

switch (defaultLevelString) {
    case "INFO":
        defaultLevel = INFO
        break
    case "DEBUG":
        defaultLevel = DEBUG
        break
    case "TRACE":
        defaultLevel = TRACE
        break
    default:
        throw new Exception("Invalid LOG_LEVEL ENV var: ${defaultLevelString} - possible options [INFO, DEBUG, TRACE]")
}

def logsDirectory = new File("./logs")

if (logsDirectory.exists()) {
    if (!logsDirectory.isDirectory()) {
        throw new Exception("Logs directory is not a directory: ${logsDirectory.getAbsolutePath()}")
    }
} else {
    logsDirectory.mkdir()
}

logsDirectory.eachFileRecurse(FileType.FILES) { file ->
    if (file.name.contains("latest")) {
        file.renameTo("./logs/" + file.name.substring("latest-".length()))
    }
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

    def date = new Date()
    def dateFormat = new SimpleDateFormat("HH-mm-ss-dd-MM-yyyy")

    file = "./logs/latest-pinguino-" + dateFormat.format(date) + ".log"
    append = false
    immediateFlush = true
}

root(defaultLevel, ["CONSOLE", "FILE"])
