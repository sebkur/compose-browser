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

import java.nio.charset.Charset
import java.nio.charset.IllegalCharsetNameException
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern

/*
 * Derived from JSoup's DataUtil
 */

val charsetPattern: Pattern = Pattern.compile("(?i)\\bcharset=\\s*(?:[\"'])?([^\\s,;\"']*)")

fun getCharsetFromContentType(contentType: String?): String? {
    if (contentType == null) {
        return null
    }
    val m: Matcher = charsetPattern.matcher(contentType)
    if (m.find()) {
        var charset: String = m.group(1).trim()
        charset = charset.replace("charset=", "")
        return validateCharset(charset)
    }
    return null
}

private fun validateCharset(charset: String?): String? {
    if (charset.isNullOrEmpty()) {
        return null
    }
    var cs: String = charset.trim { it <= ' ' }.replace("[\"']".toRegex(), "")
    try {
        if (Charset.isSupported(cs)) {
            return cs
        }
        cs = cs.uppercase(Locale.ENGLISH)
        if (Charset.isSupported(cs)) {
            return cs
        }
    } catch (e: IllegalCharsetNameException) {
        // if our this charset matching fails.... we just take the default
    }
    return null
}
