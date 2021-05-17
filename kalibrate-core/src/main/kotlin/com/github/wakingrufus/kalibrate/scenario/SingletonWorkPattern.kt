package com.github.wakingrufus.kalibrate.scenario

import com.github.wakingrufus.kalibrate.agent.Result
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow

class SingletonWorkPattern<T> : WorkPattern<T> {

    @FlowPreview
    override fun doWork(setup: (T) -> Pair<T, List<Result<*>>>, work: (T) -> List<Result<*>>): (T) -> Flow<Result<*>> {
        return { initialSession ->
            val setupResults = setup(initialSession)
            flow {
                emit(setupResults.second.asFlow())
                emit(work(setupResults.first).asFlow())
            }.flattenMerge(1000)
        }
    }
}