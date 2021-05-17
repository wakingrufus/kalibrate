package com.github.wakingrufus.kalibrate.example

import org.junit.Test

class IntegrationTest {

    @Test
    fun `test run deploy`() {
        main(arrayOf("--scenario", "deploy"))
    }

    @Test
    fun `test run load`() {
        main(arrayOf("--scenario", "load"))
    }
}