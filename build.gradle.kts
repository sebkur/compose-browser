plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    kotlin("jvm") apply false
    kotlin("multiplatform") apply false
    id("org.jetbrains.compose") apply false
    id("org.jlleitschuh.gradle.ktlint") apply false
}

tasks.wrapper {
    gradleVersion = "7.6.1"
}

allprojects {
    group = extra["pGroup"] as String
    version = extra["pVersion"] as String
}

subprojects {
    repositories {
        maven("https://mvn.topobyte.de")
        maven("https://mvn.slimjars.com")
        mavenCentral()
        google()
    }
}
