package com.grubhub.bigtestkotlin.scenario

import com.github.wakingrufus.kalibrate.agent.Success
import com.github.wakingrufus.kalibrate.scenario.Step
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Duration
import java.time.Instant

class StepTest {
    @Test
    fun `test session change`(): Unit = runBlocking {
        val step = Step<String, String> { Success(Instant.now(), Duration.ZERO, "$it result") }.apply {
            sessionChange { s, success -> success.data }
        }
        val actual = step.invoke("starting session")
        assertThat(actual.first).isEqualTo("starting session result")
        Unit
    }
}