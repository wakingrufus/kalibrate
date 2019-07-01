package com.github.wakingrufus.kalibrate.agent

import com.github.wakingrufus.kalibrate.BigTestDsl
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import mu.KLogging
import java.time.Duration
import java.time.temporal.ChronoUnit

@BigTestDsl
class KtorHttpAgent<S, R>(val client: () -> HttpClient,
                          val url: (S) -> String) {
    companion object : KLogging()

    var httpAgent: HttpAgentDsl<S, R> = HttpAgentDsl({ "" })

    fun config(config: HttpAgentDsl<S, R>.() -> Unit) {
        httpAgent = HttpAgentDsl<S, R>(url).apply(config)
    }

    suspend fun perform(session: S): HttpResponse {
        val call = httpAgent.toCall(session)
        val urlString = url(session)
        logger.debug { "invoking http agent: url=$urlString" }
        return client().request(urlString) {
            call.headers.forEach {
                header(it.first, it.second)
            }
            call.body?.also {
                body = it
            }
            method = call.method.toKtor()
        }
    }

    suspend inline fun <reified R> parseResponse(httpResponse: HttpResponse): Result<R> {
        val duration = Duration.of(httpResponse.responseTime.timestamp.minus(httpResponse.requestTime.timestamp), ChronoUnit.MILLIS)
        return when (httpResponse.status) {
            HttpStatusCode.OK -> {
                var respObj: R?
                try {
                    respObj = httpResponse.receive()
                    respObj?.let {
                        logger.debug { "success response: $respObj" }
                        Success<R>(duration, it)
                    } ?: Failure<R>("Failure to deserialize " + httpResponse.readText())
                } catch (e: Throwable) {
                    val responseString = httpResponse.readText()
                    logger.debug { "fail exception=${e.localizedMessage} status=${httpResponse.status} response=$responseString" }
                    Failure<R>("Failure to deserialize ${e.localizedMessage}")
                }
            }
            else -> Failure<R>(httpResponse.readText())
        }
    }

    suspend inline operator fun <reified R> invoke(session: S): Result<R> {
        return parseResponse(perform(session))
    }
}

fun com.github.wakingrufus.kalibrate.agent.HttpMethod.toKtor(): HttpMethod = when (this) {
    com.github.wakingrufus.kalibrate.agent.HttpMethod.GET -> HttpMethod.Get
    com.github.wakingrufus.kalibrate.agent.HttpMethod.POST -> HttpMethod.Post
    com.github.wakingrufus.kalibrate.agent.HttpMethod.DELETE -> HttpMethod.Delete
    com.github.wakingrufus.kalibrate.agent.HttpMethod.PUT -> HttpMethod.Put
}