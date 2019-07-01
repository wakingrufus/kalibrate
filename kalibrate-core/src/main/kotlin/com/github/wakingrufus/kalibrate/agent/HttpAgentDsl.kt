package com.github.wakingrufus.kalibrate.agent

import com.github.wakingrufus.kalibrate.BigTestDsl
import mu.KLogging

@BigTestDsl
class HttpAgentDsl<S, R>(val url: (S) -> String) {
    companion object : KLogging()

    var body: ((S) -> Any)? = null
    fun body(body: (S) -> Any) {
        this.body = body
    }

    var headers: MutableList<(S) -> Pair<String, String>> = mutableListOf()

    fun header(sessionHeader: (S) -> Pair<String, String>) {
        headers.add(sessionHeader)
    }

    fun toCall(session: S): HttpAgentCall<S, R> {
        return HttpAgentCall(url(session), this@HttpAgentDsl.body?.invoke(session), headers.map { it(session) }.toList())
    }
}