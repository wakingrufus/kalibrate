package com.github.wakingrufus.kalibrate.scenario

import com.github.wakingrufus.kalibrate.BigTestDsl
import com.github.wakingrufus.kalibrate.agent.Result
import com.github.wakingrufus.kalibrate.agent.Success

@BigTestDsl
class Step<S, R>(val agent: suspend (S) -> Result<R>) {
    private var sessionChange: (S, Success<R>) -> S = { session, _ -> session }
    fun sessionChange(delta: (S, Success<R>) -> S) {
        sessionChange = delta
    }

    suspend fun invoke(session: S): Pair<S, Result<R>> {
        val result = agent(session)
        val newSession = if (result is Success<R>) sessionChange(session, result) else session
        return newSession to result
    }
}
