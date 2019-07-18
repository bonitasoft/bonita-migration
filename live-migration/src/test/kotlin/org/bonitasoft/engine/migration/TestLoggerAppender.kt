package org.bonitasoft.engine.migration

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import java.util.ArrayList

class TestLoggerAppender : AppenderBase<ILoggingEvent>() {

    init {
        setInstance(this)
    }

    var events: MutableList<ILoggingEvent> = ArrayList()

    override fun append(e: ILoggingEvent) {
        events.add(e)
    }
    fun clear() {
        events.clear()
    }
    fun allLogs() : List<ILoggingEvent> {
            val allLogs: MutableList<ILoggingEvent> = events
            events = ArrayList()
            return allLogs
    }

    companion object {

        private lateinit var testLoggerAppender: TestLoggerAppender

        /**
         * Clear the recorded event list
         */
        fun clear() = testLoggerAppender.clear()

        /**
         * Get the next messages of the event list
         */
        fun allLogs(): List<ILoggingEvent> = testLoggerAppender.allLogs()

        fun setInstance(testLoggerAppender: TestLoggerAppender) {
            this.testLoggerAppender = testLoggerAppender
        }

    }

}