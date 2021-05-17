package com.github.wakingrufus.kalibrate

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.wakingrufus.kalibrate.agent.HttpAgentDsl
import com.github.wakingrufus.kalibrate.agent.KtorHttpAgent
import com.github.wakingrufus.kalibrate.scenario.Scenario
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.util.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import mu.KLogging
import java.time.Duration
import java.time.Instant

@KalibrateDsl
class KalibrateDslBuilder<T>(var sessionBuilder: (ArgParser) -> T) {
    companion object : KLogging()

    @FlowPreview
    val scenarioMap: MutableMap<String, Scenario<T>> = mutableMapOf()
    var globalHttpConfig: HttpAgentDsl<T, *>.() -> Unit = { }
    @FlowPreview
    var scenarioChooser: (T) -> String = { scenarioMap.keys.first() }
    var objectMapperConfig: ObjectMapper.() -> Unit = {
    }

    var client: HttpClient? = null

    fun sessionArgs(config: (ArgParser) -> T) {
        sessionBuilder = config
    }

    fun jackson(objectMapperConfig: ObjectMapper.() -> Unit) {
        this.objectMapperConfig = objectMapperConfig
    }

    @FlowPreview
    fun scenarioChooser(fn: (T) -> String) {
        scenarioChooser = fn
    }

    fun <R> httpAgent(
        url: (T) -> String,
        config: HttpAgentDsl<T, R>.() -> Unit = {}
    ): KtorHttpAgent<T, R> {
        return KtorHttpAgent<T, R>(client = { client!! }, url = url)
            .apply {
                config {
                    apply(this@KalibrateDslBuilder.globalHttpConfig)
                    apply(config)
                }
            }
    }

    fun globalHttpConfig(config: HttpAgentDsl<T, *>.() -> Unit) {
        globalHttpConfig = config
    }

    @FlowPreview
    fun scenario(name: String, setup: Scenario<T>.() -> Unit): Scenario<T> {
        return Scenario<T>()
            .apply(setup)
            .also { scenarioMap[name] = it }
    }

    @KtorExperimentalAPI
    @FlowPreview
    operator fun invoke(args: Array<out String>) {
        client = HttpClient(CIO) {
            install(JsonFeature) {
                serializer = JacksonSerializer(block = objectMapperConfig)
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

        val session = sessionBuilder(ArgParser(args))
        val start = Instant.now()
        val results = scenarioMap[scenarioChooser(session)]?.invoke(session) ?: flowOf()
        runBlocking {
            results.toList()
                .also { logger.info { "requests completed: ${it.size}" } }
                .also {
                    val durationSeconds = Duration.between(
                        it.map { it.timestamp }.minOrNull() ?: start,
                        it.map { it.timestamp }.maxOrNull() ?: Instant.now()
                    ).seconds
                    if (durationSeconds > 0) {
                        logger.info {
                            "req/sec = ${it.size / durationSeconds}"
                        }
                    }
                }
                .forEach { logger.debug(it.toString()) }
        }
    }
}

@KtorExperimentalAPI
@FlowPreview
fun <T> kalibrate(
    args: Array<out String>,
    sessionBuilder: (ArgParser) -> T,
    config: KalibrateDslBuilder<T>.() -> Unit
) = mainBody {
    KalibrateDslBuilder<T>(sessionBuilder).apply(config).invoke(args)
}

@DslMarker
annotation class KalibrateDsl
