import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    jvm("desktop")
    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation("com.halilibo.compose-richtext:richtext-commonmark:0.16.0") {
                    exclude(group = "org.jetbrains.skiko", module = "skiko-awt-runtime-linux-x64")
                }
                implementation("org.jsoup:jsoup:1.16.1")
            }
        }
    }
}
