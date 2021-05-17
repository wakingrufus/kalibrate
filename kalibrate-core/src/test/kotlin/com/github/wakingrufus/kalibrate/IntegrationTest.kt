package com.github.wakingrufus.kalibrate

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlinx.coroutines.FlowPreview
import mu.KLogging
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.model.HttpResponse
import org.mockserver.model.MediaType
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.random.Random

data class Session(val scenario: String = "deploy", val test: Int = 0)
data class GetResponse(val num: Int)
data class Args(val test: String)
class IntegrationTest {
    companion object : KLogging() {
        var mockServer: ClientAndServer? = null
        val port = PortHelper.freePort()

        @BeforeAll
        @JvmStatic
        fun setup() {
            mockServer = startClientAndServer(port)
            mockServer?.`when`(org.mockserver.model.HttpRequest.request())
                ?.respond(
                    HttpResponse.response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(
                            """{ "num": ${
                                Random.nextInt()
                            } }"""
                        )
                )
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            mockServer?.stop()
        }
    }

    @FlowPreview
    @Test
    fun `test run load`() {
        kalibrate(arrayOf("--scenario", "load"), { Session() }) {
            jackson {
                registerModule(KotlinModule())
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            }
            sessionArgs {
                val scenario by it.storing("scenario name")
                Session(scenario = scenario)
            }
            scenarioChooser { it.scenario }

            val get = httpAgent<GetResponse>(url = { "http://localhost:$port" })

            scenario("load") {
                simulation {
                    load(Duration.of(4, ChronoUnit.SECONDS), 100)
                    setup {
                        step<GetResponse>(get::invoke) {
                            sessionChange { session, result ->
                                session.copy(test = result.data.num).also {
                                    logger.debug { "returning new session: $it" }
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
    }
}