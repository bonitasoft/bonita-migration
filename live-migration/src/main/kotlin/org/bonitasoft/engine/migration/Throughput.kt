/*
 * Copyright (C) 2018 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 */
package org.bonitasoft.engine.migration

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit


class Throughput(private val flushInterval: Duration, private val clock: Clock = Clock.systemUTC(), private val total: Int) {

    private var countSinceLastFlush: Long = 0
    private var count: Int = 0
    private var throughput = 0.0
    private var lastFlush: Instant? = null
    private var lastIncrement: Instant? = null
    private val start: Instant = Instant.now(clock)


    val current: Double
        get() {
            flushIfNoIncrementSendDuringFlushInterval()
            return throughput
        }

    val average: Double
        get() {
            val durationSinceTheBeginning = Duration.between(start, now())
            return if (durationSinceTheBeginning.seconds > 0) {
                (count / durationSinceTheBeginning.seconds).toDouble()
            } else 0.0
        }

    val remainingTime: String
        get() {
            if (current < 0.1) {
                return "N/A"
            }
            return humanReadableDuration(((total - count) / current).toLong())
        }

    val currentDuration: String
        get() = humanReadableDuration(Duration.between(start, Instant.now(clock)).seconds)

    private fun humanReadableDuration(totalSeconds: Long): String {
        val hours = TimeUnit.SECONDS.toHours(totalSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
        val seconds = totalSeconds % 60

        return "$hours hours $minutes minutes $seconds seconds"
    }


    init {
        flush(now())
    }

    fun increment(migratedCount: Int) {
        val now = now()

        val durationSinceLastFlush = Duration.between(lastFlush!!, now)
        if (durationSinceLastFlush.compareTo(flushInterval) > 0) {
            throughput = (countSinceLastFlush / durationSinceLastFlush.seconds).toDouble()
            flush(now)
        }

        count+=migratedCount
        countSinceLastFlush+=migratedCount
        lastIncrement = now
    }

    private fun flush(now: Instant) {
        countSinceLastFlush = 0
        lastFlush = now
    }

    private fun now(): Instant {
        return Instant.now(clock)
    }

    private fun flushIfNoIncrementSendDuringFlushInterval() {
        val now = now()
        if (lastIncrement != null && lastIncrement!!.isBefore(now.minus(flushInterval))) {
            flush(now)
        }
    }


    fun getTotal(): Int {
        return this.total
    }

    fun getPercentageCompleted(): String {
        return "%.1f".format((count.toDouble() / total.toDouble()) * 100)
    }

    fun getCount(): Int {
        return this.count;
    }


}