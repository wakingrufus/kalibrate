package com.github.wakingrufus.kalibrate.scenario

import com.github.wakingrufus.kalibrate.BigTestDsl
import com.github.wakingrufus.kalibrate.agent.Result
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flow
import mu.KLogging
import java.time.Duration
import java.time.Instant

@BigTestDsl
@FlowPreview
class Simulation<T> {
    companion object : KLogging()

    private val singletonWorkPattern: suspend (session: T) -> Flow<Result<*>> = {
        coroutineScope {
            flow {
                val setupResults = runSetup(it)
                emit(setupResults.second.asFlow())
                val repeatedResults = runRepeatable(setupResults.first)
                emit(repeatedResults.asFlow())
            }.flattenConcat()
        }
    }
    private var workPattern: suspend (session: T) -> Flow<Result<*>> = singletonWorkPattern
    private var setup = StepContainer<T>()
    private var repeatable = StepContainer<T>()

    fun once() {
        workPattern = singletonWorkPattern
    }

    fun load(duration: Duration, users: Int) {
        workPattern = {
            coroutineScope {
                val setupResults = runSetup(it)
                val start = Instant.now().plusSeconds(2)
                val results = (1..users).toList().map { userId ->
                    async {
                        (0 until duration.seconds).toList().flatMap { tick ->
                            delay(start.plusSeconds(tick).toEpochMilli() - Instant.now().toEpochMilli())
                            runRepeatable(setupResults.first)
                        }
                    }
                }.map { it.await() }
                flow {
                    emit(setupResults.second.asFlow())
                    results.forEach { emit(it.asFlow()) }
                }.flattenConcat()
            }
        }
    }


    fun stress(acceleration: Int) {
        //    workPattern = { env, token -> sim(env, token) }
    }

    fun setup(setupBuilder: StepContainer<T>.() -> Unit) {
        setup = StepContainer<T>().apply(setupBuilder)
    }

    fun repeat(work: StepContainer<T>.() -> Unit) {
        repeatable = StepContainer<T>().apply(work)
    }

    suspend operator fun invoke(session: T): Flow<Result<*>> {
        return workPattern(session)
    }

    suspend fun runSetup(session: T): Pair<T, List<Result<*>>> {
        return setup(session)
    }

    suspend fun runRepeatable(session: T): List<Result<*>> {
        return repeatable(session).second
    }
}

@BigTestDsl
class StepContainer<S> {
    val steps: MutableList<Step<S, *>> = mutableListOf()

    fun <R> step(agent: (S) -> Result<R>, config: Step<S, R>.() -> Unit = {}) {
        steps.add(Step(agent).apply(config))
    }

    suspend operator fun invoke(session: S): Pair<S, List<Result<*>>> {
        return steps.fold(session to emptyList()) { pair, step ->
            step.invoke(pair.first).let {
                it.first to pair.second.plus(it.second)
            }
        }
    }
}