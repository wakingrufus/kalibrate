package com.github.wakingrufus.kalibrate.scenario

import com.github.wakingrufus.kalibrate.BigTestDsl
import com.github.wakingrufus.kalibrate.agent.Result
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow

@BigTestDsl
@FlowPreview
class Scenario<T> {
    private val simulations: MutableList<Simulation<T>> = mutableListOf()

    fun once(vararg agents: (T) -> Result<Any>): Simulation<T> {
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

    fun repeat(vararg agents: (T) -> Result<Any>): Simulation<T> {
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
        flow { simulations.map { emit(it.invoke(session)) } }.flattenMerge(1000)

}

