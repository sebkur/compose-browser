package de.mobanisto.compose.browser

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Compose Browser",
            icon = painterResource("compose-browser.png")
        ) {
            ComposeUI(initialUrl = "https://mobanisto.com")
        }
    }
}
