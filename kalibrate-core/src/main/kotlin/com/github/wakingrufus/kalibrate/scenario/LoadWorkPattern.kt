package com.github.wakingrufus.kalibrate.scenario

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import mu.KLogging
import java.time.Duration
import java.time.Instant
import com.github.wakingrufus.kalibrate.agent.Result

class LoadWorkPattern<T>(val duration: Duration, val users: Int) : WorkPattern<T> {
    companion object : KLogging()
    @OptIn(FlowPreview::class)
    override fun doWork(setup: (T) -> Pair<T, List<Result<*>>>, work: (T) -> List<Result<*>>): (T) -> Flow<Result<*>> {
        return { initialSession ->
            val start = Instant.now().plusSeconds(2)
            val setupResults = setup(initialSession)
            flowOf(setupResults.second).flatMapConcat {
                (1..users).asFlow().flatMapMerge(concurrency = 1000) { userId ->
                    (0 until duration.seconds).asFlow().map { tick ->
                        kotlinx.coroutines.delay(
                            start.plusSeconds(tick).toEpochMilli() - Instant.now().toEpochMilli()
                        )
                        logger.debug { "running tick={$tick} for user={$userId}" }
                        work(initialSession).asFlow()
                    }.flattenMerge(1000)
                }
            }
        }
    }
}