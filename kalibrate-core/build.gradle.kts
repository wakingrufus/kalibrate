plugins {
    kotlin("jvm") version "1.3.50"
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
    api("io.ktor:ktor-http-jvm:1.2.4")
    implementation("io.ktor:ktor-client-jackson:1.2.4")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")
    api("com.xenomachina:kotlin-argparser:2.0.7")
    compile("io.projectreactor.netty:reactor-netty:0.9.0.RELEASE")

    implementation("io.github.microutils:kotlin-logging:1.6.22")
    implementation ("com.xenomachina:kotlin-argparser:2.0.3")
    implementation ("com.beust:klaxon:2.1.13")
    implementation ("org.apache.commons:commons-csv:1.5")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")


    implementation("io.ktor:ktor-client-cio:1.2.4")
    api("com.github.kittinunf.fuel:fuel:2.1.0")
    implementation ("com.github.kittinunf.fuel:fuel-coroutines:2.1.0")
    implementation ("com.github.kittinunf.fuel:fuel-jackson:2.1.0")

    testCompile("junit:junit:4.12")
    testCompile("org.jetbrains.kotlin:kotlin-test")
    testCompile ("com.github.tomakehurst:wiremock:2.19.0")
    testCompile ("org.assertj:assertj-core:3.11.1")
}
