package com.github.wakingrufus.kalibrate

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.wakingrufus.kalibrate.agent.HttpAgentDsl

@KalibrateDsl
fun <S, R> HttpAgentDsl<S, R>.bodyObject(objectMapper: ObjectMapper, body: (S) -> Any) {
    this.body = { objectMapper.writeValueAsString(body(it)) }
}