package com.github.wakingrufus.kalibrate.scenario

import com.github.wakingrufus.kalibrate.agent.KtorHttpAgent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.date.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.Instant

internal class SimulationTest {
    companion object : KLogging()

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
                    connectAttempts = 5 // Maximum number of attempts for retrying a connection.
                }
            }
        }
        val get = KtorHttpAgent<String, String>(client = { client }, url = { "https://httpbin.org/get?test=${it}" })
        val sim = Simulation<String>().apply {
            repeat {
                    step<String>(get::invoke)
                    step<String>(get::invoke)
            }
        }
        runBlocking {
            val result = sim("test")
            Assertions.assertThat(result.toList(mutableListOf())).hasSize(2)
        }
    }

    @FlowPreview
    @Test
    fun `test raw`() {
        val client = HttpClient(CIO) {
            install(JsonFeature) {
                serializer = JacksonSerializer(block = { })
            }
            engine {
                maxConnectionsCount = 1000 // Maximum number of socket connections.
                endpoint.apply {
                    maxConnectionsPerRoute = 500 // Maximum number of requests for a specific endpoint route.
                    pipelineMaxSize = 20 // Max number of opened endpoints.
                    keepAliveTime = 5000 // Max number of milliseconds to keep each connection alive.
                    connectTimeout = 5000 // Number of milliseconds to wait trying to connect to the server.
                    connectAttempts = 5 // Maximum number of attempts for retrying a connection.
                }
            }
        }
        val baseStart = Instant.now().toEpochMilli()
        val flows = flow {
            (1..500).forEach { fnum ->
                emit(flow {
                    (1..3).forEach {
                        val delay =  (baseStart + (it * 1000)) - Instant.now().toEpochMilli()
                        if (delay > 0) {
                            delay(delay)
                        }
                        emit(client.get<HttpStatement>("https://httpbin.org/get?test=${fnum}-${it}").execute())

                    }
                })
            }
        }
        runBlocking {
            val list = mutableListOf<HttpResponse>()
            val fallbackStart = Instant.now().toEpochMilli()
            flows.flattenMerge(1000).collect {
                list.add(it)
                logger.info {
                    "start: ${
                        it.requestTime.toJvmDate().toInstant().toEpochMilli()
                    } end: ${it.responseTime.toJvmDate().toInstant().toEpochMilli()}"
                }
            }
            val fallbackEnd = Instant.now().toEpochMilli()
            val totalStart =
                list.map { it.requestTime.toJvmDate().toInstant().toEpochMilli() }.minOrNull() ?: fallbackStart
            val totalEnd =
                list.map { it.responseTime.toJvmDate().toInstant().toEpochMilli() }.maxOrNull() ?: fallbackEnd
            logger.info { "total requests: ${list.size}" }
            logger.info { "total duration (ms): " + (totalEnd - totalStart) }
            val rate = list.size / ((totalEnd - totalStart) / 1000)
            logger.info { "rate: $rate req/sec" }
            logger.info { list.groupBy { it.requestTime.seconds }.entries.joinToString("\n") { "${it.key}: ${it.value.size}" } }
        }
    }
}