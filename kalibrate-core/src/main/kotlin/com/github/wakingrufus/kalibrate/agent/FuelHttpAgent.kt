package com.github.wakingrufus.kalibrate.agent

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.result.map
import com.github.wakingrufus.kalibrate.BigTestDsl
import mu.KLogging
import java.time.Duration
import java.time.Instant

@BigTestDsl
class FuelHttpAgent<S, R>(val mapper: () -> ObjectMapper,
                          val url: (S) -> String) {
    companion object : KLogging()

    var httpAgent: HttpAgentDsl<S, R> = HttpAgentDsl({ "" })

    fun config(config: HttpAgentDsl<S, R>.() -> Unit) {
        httpAgent = HttpAgentDsl<S, R>(url).apply(config)
    }

    inline operator fun <reified R> invoke(session: S): Result<R> {
        httpAgent.toCall(session).let { call ->
            val urlString = call.url
            logger.debug { "invoking fuel agent: url=$urlString" }
            val timestamp = Instant.now()
            return Fuel.request(call.method.toFuel(), urlString)
                    .apply {
                        call.headers.forEach {
                            header(it.first, it.second)
                        }
                        call.body?.also {
                            body(mapper().writeValueAsString(it))
                        }
                    }

                    .response().let { triple ->
                        triple.third.map { triple.second }
                    }.fold(
                            { data -> Success<R>(timestamp, Duration.between(timestamp, Instant.now()), mapper().readValue(data.data, R::class.java)) },
                            { error ->
                                Failure<R>(timestamp, "request $call failed: $error").also {
                                    logger.debug { "Request $call Failed: $error" }
                                }
                            }
                    )
        }

    }
}

fun HttpMethod.toFuel(): Method = when (this) {
    HttpMethod.GET -> Method.GET
    HttpMethod.POST -> Method.POST
    HttpMethod.DELETE -> Method.DELETE
    HttpMethod.PUT -> Method.PUT
}