package de.mobanisto.compose.browser

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

internal expect val MARGIN_SCROLLBAR: Dp

internal expect interface ScrollbarAdapter

@Composable
internal expect fun rememberScrollbarAdapter(scrollState: LazyListState): ScrollbarAdapter

@Composable
internal expect fun VerticalScrollbar(
    modifier: Modifier,
    adapter: ScrollbarAdapter
)

@Composable
internal expect fun Dialog(
    title: String,
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit
)
