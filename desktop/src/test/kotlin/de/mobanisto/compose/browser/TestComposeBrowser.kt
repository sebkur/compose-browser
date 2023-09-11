// Copyright 2023 Sebastian Kuerten
//
// This file is part of compose-browser.
//
// compose-browser is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// compose-browser is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with compose-browser. If not, see <http://www.gnu.org/licenses/>.

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
    val version = Version.getVersion()
    println("Compose Browser version $version")
    val versionInfo = VersionInfo(version)
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Compose Browser",
            icon = painterResource("compose-browser.png")
        ) {
            window.minimumSize = DensityDimension(800, 600, density)
            window.preferredSize = DensityDimension(800, 600, density)
            CompositionLocalProvider(LocalDensity provides Density(density)) {
                ComposeUI(versionInfo, initialUrl = "about:blank", presetUrlBar = "https://mobanisto.com")
            }
        }
    }
}
