plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    kotlin("jvm") apply false
    kotlin("multiplatform") apply false
    id("org.jetbrains.compose") apply false
    id("org.jlleitschuh.gradle.ktlint") apply false
    id("de.topobyte.version-access-gradle-plugin") apply false
}

tasks.wrapper {
    gradleVersion = "7.6.3"
}

subprojects {
    repositories {
        maven("https://mvn.topobyte.de")
        mavenCentral()
        google()
    }
}
