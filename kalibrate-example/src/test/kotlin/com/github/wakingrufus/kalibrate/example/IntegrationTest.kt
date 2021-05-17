package com.github.wakingrufus.kalibrate.example

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


class IntegrationTest {

    @Test
    fun `test run deploy`() {
        main(arrayOf("--scenario", "deploy"))
    }

    @Test
    @Disabled
    fun `test run load`() {
        main(arrayOf("--scenario", "load"))
    }
}