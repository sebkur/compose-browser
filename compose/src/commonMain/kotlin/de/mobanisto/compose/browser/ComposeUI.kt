package de.mobanisto.compose.browser

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
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
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import mu.KotlinLogging
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.core5.http.HttpStatus
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

private val LOG = KotlinLogging.logger {}

fun openUrl(url: String, onResult: (String) -> Unit) {
    if (url.isBlank()) return
    LOG.info { "opening: \"$url\"" }
    if (url == "about:blank") {
        onResult(
            """
            <h2>Welcome to Compose Browser</h2>
            <p>Enter a URL above and start browsing the simple web.</p>
            """.trimMargin()
        )
        return
    }
    try {
        val client = HttpClientBuilder.create().build()
        val request = HttpGet(url)
        client.execute(request) { response ->
            if (response.code == HttpStatus.SC_OK) {
                val data = response.entity.content.use { input -> input.readBytes() }

                val contentType = response.getHeader("content-type")?.value
                val charset = getCharsetFromContentType(contentType)
                val cs = charset?.let { Charset.forName(charset) } ?: StandardCharsets.UTF_8

                val html = data.toString(cs)
                onResult(html)
            } else {
                val message = """
                    <h2>Error</h2>
                    <p>Unable to fetch the requested URL content.</p>
                    <p>Error code: ${response.code}</p>
                """.trimIndent()
                onResult(message)
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ComposeUI(
    modifier: Modifier = Modifier,
    initialUrl: String,
    presetUrlBar: String? = null,
) {
    val (url, setUrl) = remember { mutableStateOf(initialUrl) }
    val (html, setHtml) = remember {
        mutableStateOf("")
    }
    val (status, setStatus) = remember { mutableStateOf("") }
    val history = remember { History() }

    val open = { newUrl: String, addToHistory: Boolean ->
        if (addToHistory) {
            history.add(newUrl)
        }
        setUrl(newUrl)
        openUrl(newUrl) { html -> setHtml(html) }
    }

    val goBack = {
        val newUrl = history.goBack()
        open(newUrl, false)
    }

    val goForward = {
        val newUrl = history.goForward()
        open(newUrl, false)
    }

    LaunchedEffect(true) {
        open(initialUrl, true)
        presetUrlBar?.let { setUrl(it) }
    }

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
                url, setUrl, history,
                openUrl = { newUrl ->
                    open(newUrl, true)
                },
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
                open(newUrl, true)
            },
            onLinkHover = { newUrl ->
                setStatus(newUrl ?: "")
            }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AddressBar(
    url: String,
    setUrl: (String) -> Unit,
    history: History,
    openUrl: (String) -> Unit,
    goBack: () -> Unit,
    goForward: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            Button({ openUrl(url) }) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text("Browse")
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
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().padding(padding),
    ) {
        val scrollState = rememberScrollState()
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
