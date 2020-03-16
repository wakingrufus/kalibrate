package com.github.wakingrufus.kalibrate.example

import org.junit.Test

class IntegrationTest {

    @Test
    fun `test run`() {
        main(arrayOf("--scenario", "deploy"))
    }
}