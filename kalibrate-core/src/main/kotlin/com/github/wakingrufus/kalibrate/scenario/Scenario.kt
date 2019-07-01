package com.github.wakingrufus.kalibrate.scenario

import com.github.wakingrufus.kalibrate.BigTestDsl
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flattenMerge
import com.github.wakingrufus.kalibrate.agent.Result

@BigTestDsl
@FlowPreview
class Scenario<T> {
    private val simulations: MutableList<Simulation<T>> = mutableListOf()

    fun once(vararg agents: suspend (T) -> Result<Any>): Simulation<T> {
        return Simulation<T>()
                .apply {
                    setup {
                        agents.forEach {
                            this.step(it)
                        }
                    }
                }
                .also { simulations.add(it) }
    }

    fun repeat(vararg agents: suspend (T) -> Result<Any>): Simulation<T> {
        return Simulation<T>()
                .apply {
                    repeat {
                        agents.forEach {
                            this.step(it)
                        }
                    }
                }
                .also { simulations.add(it) }
    }

    fun simulation(config: Simulation<T>.() -> Unit): Simulation<T> {
        return Simulation<T>()
                .apply(config)
                .also { simulations.add(it) }
    }

    operator fun invoke(session: T): Flow<Result<*>> =
            simulations.map { it.invoke(session) }.asFlow().flattenMerge(100, 100)

}

