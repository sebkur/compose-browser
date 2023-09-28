plugins {
    java
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("de.mobanisto.pinpit")
    id("org.jlleitschuh.gradle.ktlint")
    id("de.topobyte.version-access-gradle-plugin")
}

generateVersionAccessSource {
    packageName = "de.mobanisto.compose.browser"
}

val attributeUsage = Attribute.of("org.gradle.usage", String::class.java)

val currentOs: Configuration by configurations.creating {
    extendsFrom(configurations.implementation.get())
    attributes { attribute(attributeUsage, "java-runtime") }
}

val windowsX64: Configuration by configurations.creating {
    extendsFrom(configurations.implementation.get())
    attributes { attribute(attributeUsage, "java-runtime") }
}

val linuxX64: Configuration by configurations.creating {
    extendsFrom(configurations.implementation.get())
    attributes { attribute(attributeUsage, "java-runtime") }
}

val macosX64: Configuration by configurations.creating {
    extendsFrom(configurations.implementation.get())
    attributes { attribute(attributeUsage, "java-runtime") }
}

val macosArm64: Configuration by configurations.creating {
    extendsFrom(configurations.implementation.get())
    attributes { attribute(attributeUsage, "java-runtime") }
}

sourceSets {
    main {
        java {
            compileClasspath = currentOs
            runtimeClasspath = currentOs
        }
    }
    test {
        java {
            compileClasspath += currentOs
            runtimeClasspath += currentOs
        }
    }
}

dependencies {
    implementation(project(":compose-browser-compose"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    currentOs(compose.desktop.currentOs)
    windowsX64(compose.desktop.windows_x64)
    linuxX64(compose.desktop.linux_x64)
    macosX64(compose.desktop.macos_x64)
    macosArm64(compose.desktop.macos_arm64)
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("de.topobyte:shared-preferences:0.0.1")
}

val versionCode by extra("0.1.0")
version = versionCode

pinpit.desktop {
    application {
        mainClass = "de.mobanisto.compose.browser.ComposeBrowserKt"

        nativeDistributions {
            jvmVendor = "adoptium"
            jvmVersion = "17.0.5+8"

            packageName = "Compose Browser"
            packageVersion = versionCode
            description = "Compose Browser - a simple web browser"
            vendor = "Mobanisto"
            copyright = "2023 Mobanisto"
            licenseFile.set(project.file("src/main/packaging/LICENSE.txt"))
            modules("java.naming")
            linux {
                packageName = "compose-browser"
                debMaintainer = "sebastian@mobanisto.de"
                debPackageVersion = versionCode
                appCategory = "utils"
                menuGroup = "Network;WebBrowser"
                iconFile.set(project.file("src/main/packaging/deb/compose-browser.png"))
                debPreInst.set(project.file("src/main/packaging/deb/preinst"))
                debPostInst.set(project.file("src/main/packaging/deb/postinst"))
                debPreRm.set(project.file("src/main/packaging/deb/prerm"))
                debCopyright.set(project.file("src/main/packaging/deb/copyright"))
                debLauncher.set(project.file("src/main/packaging/deb/launcher.desktop"))
                deb("UbuntuFocalX64") {
                    qualifier = "ubuntu-20.04"
                    arch = "x64"
                    depends(
                        "libc6", "libexpat1", "libgcc-s1", "libpcre3", "libuuid1", "xdg-utils",
                        "zlib1g", "libnotify4"
                    )
                }
                distributableArchive {
                    format = "tar.gz"
                    arch = "x64"
                }
            }
            windows {
                dirChooser = true
                shortcut = true
                menuGroup = "Mobanisto"
                upgradeUuid = "30B120EB-A9ED-46EB-9147-AFB5139F711D"
                packageVersion = versionCode
                iconFile.set(project.file("src/main/packaging/windows/compose-browser.ico"))
                aumid = "Mobanisto.Compose.Browser"
                msi {
                    arch = "x64"
                    bitmapBanner.set(project.file("src/main/packaging/windows/banner.bmp"))
                    bitmapDialog.set(project.file("src/main/packaging/windows/dialog.bmp"))
                }
                distributableArchive {
                    format = "zip"
                    arch = "x64"
                }
            }
            macOS {
                packageName = "Compose Browser"
                iconFile.set(project.file("../artwork/compose-browser.icns"))
                bundleID = "de.mobanisto.compose.browser"
                appCategory = "public.app-category.productivity"
                distributableArchive {
                    format = "zip"
                    arch = "x64"
                }
                distributableArchive {
                    format = "zip"
                    arch = "arm64"
                }
            }
        }
    }
}
