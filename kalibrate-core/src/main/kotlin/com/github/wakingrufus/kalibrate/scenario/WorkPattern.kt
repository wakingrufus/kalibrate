package com.github.wakingrufus.kalibrate.scenario

import com.github.wakingrufus.kalibrate.agent.Result
import kotlinx.coroutines.flow.Flow

interface WorkPattern<T> {
    fun doWork(setup: (T) -> Pair<T, List<Result<*>>>, work: (T) -> List<Result<*>>): (T) -> Flow<Result<*>>
}
