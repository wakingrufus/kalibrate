package com.github.wakingrufus.kalibrate.agent

data class HttpAgentCall<S, R>(val method: HttpMethod,
                               val url: String,
                               val body: Any? = null,
                               val headers: List<Pair<String, String>> = listOf())