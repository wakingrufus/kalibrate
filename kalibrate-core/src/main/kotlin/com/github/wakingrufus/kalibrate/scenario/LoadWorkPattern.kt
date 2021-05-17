package com.github.wakingrufus.kalibrate.scenario

import com.github.wakingrufus.kalibrate.agent.Result
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import mu.KLogging
import java.time.Duration
import java.time.Instant

class LoadWorkPattern<T>(val duration: Duration, val users: Int) : WorkPattern<T> {
    companion object : KLogging()

    @FlowPreview
    override fun doWork(setup: (T) -> Pair<T, List<Result<*>>>, work: (T) -> List<Result<*>>): (T) -> Flow<Result<*>> {
        return { initialSession ->
            val start = Instant.now().plusSeconds(2)
            val setupResults = setup(initialSession)
            flow {
                emit(setupResults.second.asFlow())
                emit((1..users).asFlow().flatMapMerge(concurrency = 10_000) { userId ->
                    (0 until duration.seconds).asFlow().map { tick ->
                        kotlinx.coroutines.delay(
                            start.plusSeconds(tick).toEpochMilli() - Instant.now().toEpochMilli()
                        )
                        work(initialSession).asFlow()
                    }.flattenMerge(100)
                })
            }.flattenMerge(1_000)
        }
    }
}