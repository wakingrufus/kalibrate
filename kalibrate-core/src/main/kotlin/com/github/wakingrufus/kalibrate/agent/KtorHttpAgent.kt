package com.github.wakingrufus.kalibrate.agent

import com.github.wakingrufus.kalibrate.KalibrateDsl
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.HttpMethod
import kotlinx.coroutines.runBlocking
import mu.KLogging
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@KalibrateDsl
class KtorHttpAgent<S, R>(
    val client: () -> HttpClient,
    val url: (S) -> String
) {
    companion object : KLogging()

    var httpAgent: HttpAgentDsl<S, R> = HttpAgentDsl({ "" })

    fun config(config: HttpAgentDsl<S, R>.() -> Unit) {
        httpAgent = HttpAgentDsl<S, R>(url).apply(config)
    }

    suspend fun perform(session: S): HttpStatement {
        val call = httpAgent.toCall(session)
        val urlString = url(session)
        logger.info { "invoking http agent: url=$urlString" }
        return client().request<HttpStatement>(urlString) {
            call.headers.forEach {
                header(it.first, it.second)
            }
            call.body?.also {
                body = it
            }
            method = call.method.toKtor()
        }
    }

    suspend inline fun <reified R> parseResponse(statement: HttpStatement): Result<R> {
        return statement.receive<HttpResponse, Result<R>> { httpResponse ->
            val duration = Duration.of(
                httpResponse.responseTime.timestamp.minus(httpResponse.requestTime.timestamp),
                ChronoUnit.MILLIS
            )
            val startTime = Instant.ofEpochMilli(httpResponse.requestTime.timestamp)
            when (httpResponse.status) {
                HttpStatusCode.OK -> {
                    val respObj: R?
                    try {
                        respObj = httpResponse.receive()
                        respObj?.let {
                            logger.debug { "success response: $respObj" }
                            Success<R>(startTime, duration, it)
                        } ?: Failure<R>(startTime, "Failure to deserialize")
                    } catch (e: Throwable) {
                        logger.debug { "fail exception=${e.localizedMessage} status=${httpResponse.status}" }
                        Failure<R>(startTime, "Failure to deserialize ${e.localizedMessage}")
                    }
                }
                else -> Failure<R>(startTime, httpResponse.readText())
            }
        }
    }

    inline operator fun <reified R> invoke(session: S): Result<R> {
        return runBlocking { parseResponse(perform(session)) }
    }
}

fun com.github.wakingrufus.kalibrate.agent.HttpMethod.toKtor(): HttpMethod = when (this) {
    com.github.wakingrufus.kalibrate.agent.HttpMethod.GET -> HttpMethod.Get
    com.github.wakingrufus.kalibrate.agent.HttpMethod.POST -> HttpMethod.Post
    com.github.wakingrufus.kalibrate.agent.HttpMethod.DELETE -> HttpMethod.Delete
    com.github.wakingrufus.kalibrate.agent.HttpMethod.PUT -> HttpMethod.Put
}