import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
    maven
}

repositories {
    mavenCentral()
    jcenter()
}

java.sourceCompatibility = JavaVersion.VERSION_1_8

dependencies {
    api("org.slf4j:slf4j-api:1.7.25")
    api("com.google.code.findbugs:findbugs-annotations:3.0.1")
    api("io.ktor:ktor-http-jvm:1.5.4")
    implementation("io.ktor:ktor-client-jackson:1.5.4")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.4")
    api("com.xenomachina:kotlin-argparser:2.0.7")
    compile("io.projectreactor.netty:reactor-netty:0.9.0.RELEASE")

    implementation("io.github.microutils:kotlin-logging:1.6.22")
    implementation ("com.xenomachina:kotlin-argparser:2.0.3")
    implementation ("com.beust:klaxon:2.1.13")
    implementation ("org.apache.commons:commons-csv:1.5")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core")


    implementation("io.ktor:ktor-client-cio:1.5.4")
    api("com.github.kittinunf.fuel:fuel:2.1.0")
    implementation ("com.github.kittinunf.fuel:fuel-coroutines:2.1.0")
    implementation ("com.github.kittinunf.fuel:fuel-jackson:2.1.0")

    testCompile("junit:junit:4.13.1")
    testCompile("org.jetbrains.kotlin:kotlin-test")
    testCompile ("com.github.tomakehurst:wiremock:2.19.0")
    testCompile ("org.assertj:assertj-core:3.11.1")
}
val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    languageVersion = "1.4"
}