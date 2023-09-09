package de.mobanisto.compose.browser

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

private val green1 = Color(0xFF41C300)
private val green2 = Color(0xFF008B00)

val LightColors = lightColors(
    primary = green1,
    primaryVariant = green2,
)

val DarkColors = darkColors(
    primary = green1,
    primaryVariant = green1,
)
