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

private fun validateCharset(cs: String): String? {
    var cs: String? = cs
    if (cs == null || cs.length == 0) {
        return null
    }
    cs = cs.trim { it <= ' ' }.replace("[\"']".toRegex(), "")
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
