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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.core5.http.ClassicHttpResponse
import org.apache.hc.core5.http.HttpStatus
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

private val LOG = KotlinLogging.logger {}

fun openUrl(versionInfo: VersionInfo, url: String, onResult: (String) -> Unit, onRedirect: (String) -> Unit) {
    if (url.isNotBlank()) {
        LOG.info { "opening: \"$url\"" }
    }
    if (url.isBlank() || url == "about:blank") {
        onResult(
            """
            <h2>Welcome to Compose Browser</h2>
            <p>Enter a URL above and start browsing the simple web.</p>
            <p>Or find out more about this software at <a href="about:credits">about:credits</a>.</p>
            """.trimMargin()
        )
    } else if (url == "about:credits") {
        onResult(
            """
            <h2>About Compose Browser ${versionInfo.version}</h2>
            <p>This Browser is built using Compose for Desktop and is written in Kotlin.</p>
            <p>I would like to thank:</p>
            <ul>
              <li>jeremyrempel
              <li>ialokim
            </ul>
            """.trimMargin()
        )
    } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
        val encoded = URLEncoder.encode(url, "UTF-8")
        onResult(
            """
            <h2>What now?</h2>
            <p>The address your entered does not start with 'http://' or 'https://'.</p>
            <p>Do you want to search '$url' on one of these search engines?</p>
            <ul>
            <li><a href="https://html.duckduckgo.com/html/?q=$encoded">DuckDuckGo</a></li>
            <li><a href="https://www.startpage.com/search?q=$encoded">StartPage</a></li>
            </ul>
            """.trimMargin()
        )
    } else try {
        val client = HttpClientBuilder.create()
            .setUserAgent("Compose Browser/0.0.1").disableRedirectHandling().build()
        val request = HttpGet(url)
        client.execute(request) { response ->
            println(response.code)
            when (response.code) {
                HttpStatus.SC_OK -> {
                    showResult(response, onResult)
                }

                HttpStatus.SC_MOVED_PERMANENTLY, HttpStatus.SC_MOVED_TEMPORARILY -> {
                    val location = response.getFirstHeader("location")?.value
                    if (location != null) {
                        onRedirect(location)
                    } else {
                        showResult(response, onResult)
                    }
                }

                else -> {
                    val message = """
                            <h2>Error</h2>
                            <p>Unable to fetch the requested URL content.</p>
                            <p>Error code: ${response.code}</p>
                    """.trimIndent()
                    onResult(message)
                }
            }
        }
    } catch (e: Throwable) {
        val message = """
            <h2>Error</h2>
            <p>Unable to fetch the requested URL content.</p>
            <p>Error message: ${e.message}</p>
        """.trimIndent()
        onResult(message)
    }
}

fun showResult(response: ClassicHttpResponse, onResult: (String) -> Unit) {
    val data = response.entity.content.use { input -> input.readBytes() }

    val contentType = response.getFirstHeader("content-type")?.value
    val charset = getCharsetFromContentType(contentType)
    val cs = charset?.let { Charset.forName(charset) } ?: StandardCharsets.UTF_8

    val html = data.toString(cs)
    onResult(html)
}

fun open(
    versionInfo: VersionInfo,
    history: History,
    setUrl: (String) -> Unit,
    setLoading: (Boolean) -> Unit,
    coroutineScope: CoroutineScope,
    setHtml: (String) -> Unit,
    newUrl: String,
    addToHistory: Boolean
) {
    if (addToHistory) {
        history.add(newUrl)
    }
    setUrl(newUrl)
    setLoading(true)
    coroutineScope.launch(Dispatchers.IO) {
        openUrl(
            versionInfo,
            newUrl,
            onResult = { html ->
                val doc = Jsoup.parse(html)
                handleMetaRedirects(versionInfo, history, setUrl, setLoading, coroutineScope, setHtml, doc)
                setHtml(html)
                setLoading(false)
            },
            onRedirect = { location ->
                LOG.info { "redirecting to $location" }
                open(versionInfo, history, setUrl, setLoading, coroutineScope, setHtml, location, false)
            }
        )
    }
    Unit
}

fun handleMetaRedirects(
    versionInfo: VersionInfo,
    history: History,
    setUrl: (String) -> Unit,
    setLoading: (Boolean) -> Unit,
    coroutineScope: CoroutineScope,
    setHtml: (String) -> Unit,
    doc: Document
) {
    val meta = doc.getElementsByTag("meta")
    val refresh = meta.firstOrNull { m -> m.attr("http-equiv").lowercase() == "refresh" }
    if (refresh != null) {
        val values = refresh.attr("content").lowercase().split(";")
        if (values.size > 1 && values[1].startsWith("url=")) {
            val location = values[1].removePrefix("url=")
            LOG.info { "redirecting to $location" }
            open(versionInfo, history, setUrl, setLoading, coroutineScope, setHtml, location, false)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ComposeUI(
    versionInfo: VersionInfo,
    modifier: Modifier = Modifier,
    initialUrl: String,
    presetUrlBar: String? = null,
) {
    val (url, setUrl) = remember { mutableStateOf(initialUrl) }
    val (status, setStatus) = remember { mutableStateOf("") }
    val (html, setHtml) = remember {
        mutableStateOf("")
    }
    val history = remember { History() }
    val (loading, setLoading) = remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val coroutineScope = rememberCoroutineScope()

    val setHtmlAndScrollToTop: (String) -> Unit = {
        setHtml(it)
        coroutineScope.launch {
            scrollState.scrollTo(0)
        }
    }

    val goBack = {
        val newUrl = history.goBack()
        open(versionInfo, history, setUrl, setLoading, coroutineScope, setHtmlAndScrollToTop, newUrl, false)
    }

    val goForward = {
        val newUrl = history.goForward()
        open(versionInfo, history, setUrl, setLoading, coroutineScope, setHtmlAndScrollToTop, newUrl, false)
    }

    LaunchedEffect(true) {
        open(versionInfo, history, setUrl, setLoading, coroutineScope, setHtmlAndScrollToTop, initialUrl, true)
        presetUrlBar?.let { setUrl(it) }
    }

    val dark = remember { mutableStateOf(false) }

    MaterialTheme(
        typography = CustomTypography(FontName.NOTO), // TODO: make user-selectable
        colors = if (dark.value) DarkColors else LightColors,
    ) {
        Scaffold(
            // NOTE: PointerButton.Back and PointerButton.Forward use indexes 3 and 4
            //   which does not seem to work on my machine and with my mouse :/
            modifier.onClick(matcher = PointerMatcher.mouse(PointerButton(5))) {
                if (history.canGoBack()) goBack()
            }.onClick(matcher = PointerMatcher.mouse(PointerButton(6))) {
                if (history.canGoForward()) goForward()
            },
            topBar = {
                AddressBar(
                    url, setUrl, history, loading,
                    openUrl = { newUrl ->
                        open(
                            versionInfo,
                            history,
                            setUrl,
                            setLoading,
                            coroutineScope,
                            setHtmlAndScrollToTop,
                            newUrl,
                            true
                        )
                    },
                    dark = dark,
                    goBack = goBack,
                    goForward = goForward,
                )
            },
            bottomBar = {
                Text(status)
            }
        ) { padding ->
            Content(
                padding, html, url,
                onLinkClicked = { newUrl ->
                    open(versionInfo, history, setUrl, setLoading, coroutineScope, setHtmlAndScrollToTop, newUrl, true)
                },
                onLinkHover = { newUrl ->
                    setStatus(newUrl ?: "")
                },
                scrollState = scrollState
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AddressBar(
    url: String,
    setUrl: (String) -> Unit,
    history: History,
    loading: Boolean,
    dark: MutableState<Boolean>,
    openUrl: (String) -> Unit,
    goBack: () -> Unit,
    goForward: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(goBack, enabled = history.canGoBack()) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            Button(goForward, enabled = history.canGoForward()) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            OutlinedTextField(
                url,
                setUrl,
                singleLine = true,
                placeholder = { Text("Type a URL") },
                modifier = Modifier.weight(1f, true).onPreviewKeyEvent {
                    if (it.type == KeyEventType.KeyDown && it.key == Key.Enter) {
                        openUrl(url)
                        return@onPreviewKeyEvent true
                    }
                    false
                }
            )
            if (loading) {
                CircularProgressIndicator()
            } else {
                CircularProgressIndicator(1f)
            }
            Button({ openUrl(url) }) {
                Icon(
                    imageVector = Icons.Filled.KeyboardReturn,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text("Browse")
            }
            Button({ dark.value = !dark.value }) {
                Icon(
                    imageVector = if (dark.value) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
                )
            }
        }
        Divider()
    }
}

@Composable
private fun Content(
    padding: PaddingValues,
    html: String,
    baseUri: String,
    onLinkHover: (String?) -> Unit,
    onLinkClicked: (String) -> Unit,
    scrollState: ScrollState,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().padding(padding),
    ) {
        SelectionContainer(
            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState).padding(16.dp),
        ) {
            HtmlText(
                html, baseUri,
                handleLink = {
                    onLinkClicked(it)
                },
                hoverLink = {
                    onLinkHover(it)
                }
            )
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        )
    }
}
