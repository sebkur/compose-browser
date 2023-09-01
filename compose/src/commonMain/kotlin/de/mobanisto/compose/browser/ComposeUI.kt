package de.mobanisto.compose.browser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import java.net.URL
import java.nio.charset.StandardCharsets

fun openUrl(url: String, onResult: (String) -> Unit) {
    if (url.isBlank()) return
    println("opening: \"$url\"")
    try {
        URL(url).openStream().use {
            val data = it.readBytes()
            val html = data.toString(StandardCharsets.UTF_8)
            onResult.invoke(html)
        }
    } catch (e: Throwable) {
        val message = """
            <h2>Error</h2>
            <p>Unable to fetch the requested URL content.</p>
            <p>Error message: ${e.message}</p>
        """.trimIndent()
        onResult.invoke(message)
    }
}

@Composable
fun ComposeUI(
    modifier: Modifier = Modifier,
    initialUrl: String,
) {
    val (url, setUrl) = remember { mutableStateOf(initialUrl) }
    val (html, setHtml) = remember {
        mutableStateOf(
            """
                <h2>Welcome to Compose Browser</h2>
                <p>Enter a URL above and start browsing the simple web.</p>
                """.trimMargin()
        )
    }
    val (status, setStatus) = remember { mutableStateOf("") }

    val open = { newUrl: String ->
        setUrl(newUrl)
        openUrl(newUrl) { html -> setHtml(html) }
    }

    Scaffold(
        modifier,
        topBar = {
            UrlInput(url, setUrl) { newUrl ->
                open(newUrl)
            }
        },
        bottomBar = {
            Text(status)
        }
    ) { padding ->
        Content(
            padding, html, url,
            onLinkClicked = { newUrl ->
                open(newUrl)
            },
            onLinkHover = { newUrl ->
                setStatus(newUrl ?: "")
            }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun UrlInput(url: String, setUrl: (String) -> Unit, openUrl: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(
                url,
                setUrl,
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
        androidx.compose.foundation.VerticalScrollbar(
            adapter = androidx.compose.foundation.rememberScrollbarAdapter(scrollState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        )
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
    }
}
