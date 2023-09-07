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
