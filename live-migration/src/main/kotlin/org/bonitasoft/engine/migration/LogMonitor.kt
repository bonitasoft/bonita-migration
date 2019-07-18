package org.bonitasoft.engine.migration

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class LogMonitor {
    var throughput: Throughput? = null
    val logger = LoggerFactory.getLogger(LogMonitor::class.java)
    @Scheduled(fixedDelay = 10000)
    fun log() {
        if (throughput != null) {
            logger.info("Migrated ${throughput?.getCount()} elements" +
                    " (${throughput?.getPercentageCompleted()}%)," +
                    " throughput is ${throughput?.current} elements/second, " +
                    "${throughput?.remainingTime} remaining.")
        }
    }

    fun registerCounter(throughput: Throughput) {
        this.throughput = throughput
    }

}