package com.github.wakingrufus.kalibrate.scenario

import com.github.wakingrufus.kalibrate.KalibrateDsl
import com.github.wakingrufus.kalibrate.agent.KtorHttpAgent
import com.github.wakingrufus.kalibrate.agent.Result
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import mu.KLogging
import java.time.Duration

@KalibrateDsl
@FlowPreview
class Simulation<T> {
    companion object : KLogging()

    private var workPattern: WorkPattern<T> = SingletonWorkPattern<T>()
    private var setup = StepContainer<T>()
    private var repeatable = StepContainer<T>()

    fun once() {
        workPattern = SingletonWorkPattern<T>()
    }

    fun load(duration: Duration, users: Int) {
        workPattern = LoadWorkPattern<T>(duration, users)
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

    operator fun invoke(session: T): Flow<Result<*>> {
        return workPattern.doWork(setup = setup::invoke) { repeatable.invoke(it).second }.invoke(session)
    }

    fun runSetup(session: T): Pair<T, List<Result<*>>> {
        return setup(session)
    }

    fun runRepeatable(session: T): List<Result<*>> {
        return repeatable(session).second
    }
}

@KalibrateDsl
class StepContainer<S> {
    val steps: MutableList<Step<S, *>> = mutableListOf()

    fun <R> step(agent: (S) -> Result<R>, config: Step<S, R>.() -> Unit = {}) {
        steps.add(Step(agent).apply(config))
    }

    operator fun invoke(session: S): Pair<S, List<Result<*>>> {
        return steps.fold(session to emptyList()) { pair, step ->
            step.invoke(pair.first).let {
                it.first to pair.second.plus(it.second)
            }
        }
    }
}
