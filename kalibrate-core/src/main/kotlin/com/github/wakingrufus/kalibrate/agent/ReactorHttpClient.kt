package com.github.wakingrufus.kalibrate.agent

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.wakingrufus.kalibrate.BigTestDsl
import mu.KLogging
import reactor.core.publisher.Mono
import reactor.netty.ByteBufFlux
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.time.Instant

val reactorClient = HttpClient.create()

@BigTestDsl
class ReactorHttpAgent<S, R>(val mapper: () -> ObjectMapper, val url: (S) -> String) {
    companion object : KLogging()

    var httpAgent: HttpAgentDsl<S, R> = HttpAgentDsl({ "" })
    fun config(config: HttpAgentDsl<S, R>.() -> Unit) {
        httpAgent = HttpAgentDsl<S, R>(url).apply(config)
    }

    inline operator fun <reified R> invoke(session: S): Result<R> {
        return reactorClient
                .headers { h ->
                    httpAgent.headers.map { it(session) }.forEach {
                        h.add(it.first, it.second)
                    }
                }
                .let {
                    when (httpAgent.method) {
                        HttpMethod.GET -> it.get()
                        HttpMethod.POST -> it.post().let { c ->
                            httpAgent.body?.let { c.send(ByteBufFlux.fromString(Mono.just(mapper().writeValueAsString(it(session))))) }
                                    ?: c
                        }
                        HttpMethod.PUT -> it.put()
                        HttpMethod.DELETE -> it.delete()
                    }
                }
                .uri(httpAgent.url(session))
                .responseSingle<Result<R>> { t, u ->
                    u.asString()
                            .map { mapper().readValue(it, R::class.java) }
                            .map { Success(Instant.now(), Duration.ZERO, data = it) }
                }.blockOptional().orElse(Failure<R>(Instant.now(), "error"))
    }
}