package com.github.wakingrufus.kalibrate.scenario

import com.github.wakingrufus.kalibrate.agent.KtorHttpAgent
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Test

internal class SimulationTest {
    @FlowPreview
    @Test
    fun test() {
        val client = HttpClient(CIO) {
            install(JsonFeature) {
                serializer = JacksonSerializer(block = { })
            }
            engine {
                maxConnectionsCount = 1000 // Maximum number of socket connections.
                endpoint.apply {
                    maxConnectionsPerRoute = 100 // Maximum number of requests for a specific endpoint route.
                    pipelineMaxSize = 20 // Max number of opened endpoints.
                    keepAliveTime = 5000 // Max number of milliseconds to keep each connection alive.
                    connectTimeout = 5000 // Number of milliseconds to wait trying to connect to the server.
                    connectRetryAttempts = 5 // Maximum number of attempts for retrying a connection.
                }
            }
        }
        val get = KtorHttpAgent<String, String>(client = { client }, url = { "https://httpbin.org/get?test=${it}" })
        val sim = Simulation<String>().apply {
            repeat {
                runBlocking {
                    step<String>(get::invoke)
                }
                runBlocking {
                    step<String>(get::invoke)
                }
            }
        }
        runBlocking {
            val result = sim("test")
            Assertions.assertThat(result.toList(mutableListOf())).hasSize(2)
        }
    }
}