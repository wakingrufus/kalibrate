package com.github.wakingrufus.kalibrate.scenario

import com.github.wakingrufus.kalibrate.agent.Result
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge

class SingletonWorkPattern<T> : WorkPattern<T> {
    @OptIn(FlowPreview::class)
    override fun doWork(setup: (T) -> Pair<T, List<Result<*>>>, work: (T) -> List<Result<*>>): (T) -> Flow<Result<*>> {
        return { initialSession ->
            val setupResults = setup(initialSession)
            setupResults.second.asFlow().flatMapMerge(1000) {
                work(setupResults.first).asFlow()
            }
        }
    }
}