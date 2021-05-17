package com.github.wakingrufus.kalibrate.example

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.wakingrufus.kalibrate.kalibrate
import com.xenomachina.argparser.default
import io.ktor.util.*
import kotlinx.coroutines.FlowPreview
import mu.KLogging
import java.time.Duration
import java.time.temporal.ChronoUnit

val mainLogger = KLogging().logger("main")

@FlowPreview
@KtorExperimentalAPI
fun main(args: Array<String>) = kalibrate(args, { Session() }) {
    jackson {
        registerModule(KotlinModule())
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }
    sessionArgs {
        val scenario by it.storing("scenario name")
        val baseUrl: String by it.storing(
            "-u", "--url",
            help = "base url to use for requests"
        ).default("https://httpbin.org/get")
        Session(scenario = scenario, baseUrl = baseUrl)
    }
    scenarioChooser { it.scenario }

    val get = httpAgent<GetResponse>(url = { "${it.baseUrl}?test=${it.test}" })

    scenario("deploy") {
        simulation {
            once()
            setup {
                step<GetResponse>(get::invoke) {
                    sessionChange { session, result ->
                        session.copy(test = result.data.args.test).also {
                            mainLogger.debug { "returning new session: $it" }
                        }
                    }
                }
            }
            repeat {
                step<GetResponse>(get::invoke)
            }
        }
    }
    scenario("load") {
        simulation {
            load(Duration.of(4, ChronoUnit.SECONDS), 400)
            setup {
                step<GetResponse>(get::invoke) {
                    sessionChange { session, result ->
                        session.copy(test = result.data.args.test).also {
                            mainLogger.debug { "returning new session: $it" }
                        }
                    }
                }
            }
            repeat {
                step<GetResponse>(get::invoke)
            }
        }
    }
}

data class Session(
    val scenario: String = "deploy",
    val test: String = "start",
    val baseUrl: String = ""
)