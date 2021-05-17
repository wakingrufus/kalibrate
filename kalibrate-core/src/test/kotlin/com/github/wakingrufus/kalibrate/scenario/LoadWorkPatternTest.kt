package com.github.wakingrufus.kalibrate.scenario

import com.github.wakingrufus.kalibrate.agent.Success
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

internal class LoadWorkPatternTest {
    companion object : KLogging()

    @Test
    fun test() {
        val pattern = LoadWorkPattern<Int>(Duration.ofSeconds(10), 10)
        val results = pattern.doWork(setup = { 1 to listOf(Success(Instant.now(), Duration.ZERO, 1)) }) {
            listOf(Success(Instant.now(), Duration.ZERO, (it + 1)))
        }
        runBlocking {
            results.invoke(0).toList().forEach {
                logger.info { it }
            }
        }
    }
}