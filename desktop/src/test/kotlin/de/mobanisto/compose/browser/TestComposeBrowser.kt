package de.mobanisto.compose.browser

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.topobyte.shared.preferences.SharedPreferences

fun main() {
    val density = SharedPreferences.getUIScale().toFloat()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Compose Browser",
            icon = painterResource("compose-browser.png")
        ) {
            window.minimumSize = DensityDimension(800, 600, density)
            window.preferredSize = DensityDimension(800, 600, density)
            CompositionLocalProvider(LocalDensity provides Density(density)) {
                ComposeUI(initialUrl = "about:blank", presetUrlBar = "https://mobanisto.com")
            }
        }
    }
}
