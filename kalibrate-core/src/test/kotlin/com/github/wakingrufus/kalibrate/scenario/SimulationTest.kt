package com.github.wakingrufus.kalibrate.scenario

import com.github.wakingrufus.kalibrate.agent.Success
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

internal class SimulationTest {
    companion object : KLogging()

    @FlowPreview
    @Test
    fun `test no setup`() {
        val sim = Simulation<String>().apply {
            once()
            repeat {
                step({ Success(Instant.now(), Duration.ZERO, "1") })
                step({ Success(Instant.now(), Duration.ZERO, "1") })
            }
        }
        runBlocking {
            val result = sim("test").toList()
            Assertions.assertThat(result).hasSize(2)
        }
    }

    @FlowPreview
    @Test
    fun `test once`() {
        val sim = Simulation<String>().apply {
            once()
            setup {
                step({ Success(Instant.now(), Duration.ZERO, "1") })
                step({ Success(Instant.now(), Duration.ZERO, "1") })
            }
            repeat {
                step({ Success(Instant.now(), Duration.ZERO, "1") })
                step({ Success(Instant.now(), Duration.ZERO, "1") })
            }
        }
        runBlocking {
            val result = sim("test").toList()
            Assertions.assertThat(result).hasSize(4)
        }
    }

    @FlowPreview
    @Test
    fun `test load`() {
        val sim = Simulation<String>().apply {
            load(Duration.ofSeconds(1), 2)
            repeat {
                step({ Success(Instant.now(), Duration.ZERO, "1") })
                step({ Success(Instant.now(), Duration.ZERO, "1") })
            }
        }
        runBlocking {
            val result = sim("test").toList()
            Assertions.assertThat(result).hasSize(4)
        }
    }

    @FlowPreview
    @Test
    fun `test high load`() {
        val duration = 3L
        val users = 250
        val steps = 10
        val sim = Simulation<String>().apply {
            load(Duration.ofSeconds(duration), users)
            setup {
            }
            repeat {
                step({
                    runBlocking {
                        delay(Random.nextLong(100))
                        Success(Instant.now(), Duration.ZERO, "1")
                    }
                })
                (1 until steps).forEach {
                    step({
                        runBlocking {
                            Success(Instant.now(), Duration.ZERO, "1")
                        }
                    })
                }
            }
        }
        runBlocking {
            val start = Instant.now().plusSeconds(2)
            val result = sim("test").toList()
            val end = Instant.now()
            Assertions.assertThat(result).hasSize(duration.toInt() * users * steps)
            logger.info { result.size }
            val actualDuration = Duration.between(start, end)
            logger.info { "Duration: $actualDuration" }
            logger.info { "Rate: ${result.size / actualDuration.seconds} req/sec" }
        }
    }
}