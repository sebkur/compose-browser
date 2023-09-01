pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
    }

    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        val composeVersion = extra["compose.version"] as String
        val pinpitVersion = extra["pinpit.version"] as String
        val ktlintVersion = extra["ktlint.version"] as String

        kotlin("jvm").version(kotlinVersion)
        kotlin("multiplatform").version(kotlinVersion)
        id("org.jetbrains.compose").version(composeVersion)
        id("de.mobanisto.pinpit").version(pinpitVersion)
        id("org.jlleitschuh.gradle.ktlint").version(ktlintVersion)
    }
}

include("compose", "desktop")
project(":compose").name = "compose-browser-compose"
project(":desktop").name = "compose-browser-desktop"
