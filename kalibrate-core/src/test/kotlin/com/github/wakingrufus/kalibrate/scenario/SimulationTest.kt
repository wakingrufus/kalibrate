package com.github.wakingrufus.kalibrate.scenario

import com.github.wakingrufus.kalibrate.agent.Success
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

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
}