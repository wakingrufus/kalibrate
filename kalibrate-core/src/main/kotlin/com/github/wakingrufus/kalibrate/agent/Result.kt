package com.github.wakingrufus.kalibrate.agent

import java.time.Duration
import java.time.Instant

sealed class Result<T>(open val timestamp: Instant)

data class Success<T>(override val timestamp: Instant, val duration: Duration, val data: T) : Result<T>(timestamp)
data class Failure<T>(override val timestamp: Instant, val reason: String) : Result<T>(timestamp)