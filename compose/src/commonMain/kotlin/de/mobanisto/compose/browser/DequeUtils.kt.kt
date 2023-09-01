package de.mobanisto.compose.browser

fun <E> ArrayDeque<E>.push(e: E) {
    addLast(e)
}

fun <E> ArrayDeque<E>.pop(): E {
    return removeLast()
}

fun <E> ArrayDeque<E>.top(): E {
    return last()
}
