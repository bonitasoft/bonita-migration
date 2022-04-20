/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.update.core

import java.text.SimpleDateFormat

import static java.util.Optional.ofNullable

import org.slf4j.LoggerFactory

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender

class LoggingConfiguration {

    static void initializeConfiguration() {
        def rootLogger = LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger
        LoggerContext loggerContext = rootLogger.getLoggerContext()

        def updateLogger = loggerContext.getLogger(Logger.class)
        updateLogger.setLevel(Level.INFO)
        updateLogger.setAdditive(false) // we do not want this logger to use appender inherited from root logger

        addConsoleAppenderIfNotAvailable(loggerContext, updateLogger)
        addFileAppenderIfNotAvailable(loggerContext, updateLogger)
    }

    static void configureUpdateLogger(String level) {
        def updateLogger = LoggerFactory.getLogger(Logger.class) as ch.qos.logback.classic.Logger
        updateLogger.setLevel(toLogbackLogLevel(level))
    }

    private static Level toLogbackLogLevel(String level) {
        try {
            return Level.valueOf(ofNullable(level).orElse("DEBUG"))
        }
        catch (Exception ignored) {
            println "[WARN] logging configuration issue, unknown level: ${level}. Use DEBUG instead"
            return Level.DEBUG
        }
    }

    private static void addConsoleAppenderIfNotAvailable(LoggerContext loggerContext, def logger) {
        final String appenderName = 'update-console'
        if (logger.getAppender(appenderName) == null) {
            PatternLayoutEncoder consolePatternLayoutEncoder = new PatternLayoutEncoder()
            consolePatternLayoutEncoder.setPattern("[%level] %msg%n")
            consolePatternLayoutEncoder.setContext(loggerContext)
            consolePatternLayoutEncoder.start()
            ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<ILoggingEvent>()
            consoleAppender.setName(appenderName)
            consoleAppender.setContext(loggerContext)
            consoleAppender.setEncoder(consolePatternLayoutEncoder)
            consoleAppender.start()

            logger.addAppender(consoleAppender)
        }
    }

    private static void addFileAppenderIfNotAvailable(LoggerContext loggerContext, def logger) {
        final String appenderName = 'update-file'
        if (logger.getAppender(appenderName) == null) {
            PatternLayoutEncoder filePatternLayoutEncoder = new PatternLayoutEncoder()
            filePatternLayoutEncoder.setPattern("%date{yyyy-MM-dd-HH:mm:ss.SSSXXX} [%level] %msg%n")
            filePatternLayoutEncoder.setContext(loggerContext)
            filePatternLayoutEncoder.start()
            FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>()
            fileAppender.setName(appenderName)
            fileAppender.setFile(new File("bonita-update-" + new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date()) + ".log").getAbsolutePath())
            fileAppender.setEncoder(filePatternLayoutEncoder)
            fileAppender.setContext(loggerContext)
            fileAppender.start()

            logger.addAppender(fileAppender)
        }
    }

}
