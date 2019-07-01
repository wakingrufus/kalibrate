package com.github.wakingrufus.kalibrate.agent

import java.time.Duration

sealed class Result<T>

data class Success<T>(val duration: Duration, val data: T) : Result<T>()
data class Failure<T>(val reason: String) : Result<T>()