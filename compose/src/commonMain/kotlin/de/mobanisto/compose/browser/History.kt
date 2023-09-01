package de.mobanisto.compose.browser

import androidx.compose.runtime.mutableStateOf

class History {

    private var position = mutableStateOf(-1)
    private val entries = mutableStateOf(ArrayDeque<String>())

    fun add(url: String) {
        while (position.value < entries.value.size - 1) {
            entries.value.pop()
        }
        entries.value.add(url)
        position.value += 1
    }

    fun canGoForward(): Boolean {
        return entries.value.isNotEmpty() && position.value < entries.value.size - 1
    }

    fun canGoBack(): Boolean {
        return entries.value.isNotEmpty() && position.value > 0
    }

    fun goForward(): String {
        position.value += 1
        return entries.value[position.value]
    }

    fun goBack(): String {
        position.value -= 1
        return entries.value[position.value]
    }
}
