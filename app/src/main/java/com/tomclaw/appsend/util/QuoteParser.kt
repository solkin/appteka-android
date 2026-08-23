package com.tomclaw.appsend.util

/** A message line with its quote nesting depth: zero is plain text, one and above is a quote. */
data class QuoteLine(val depth: Int, val text: String)

fun String.isQuoteLine(): Boolean = startsWith(QUOTE_MARKER)

fun String.hasQuote(): Boolean = QUOTE_MARKER in this && lines().any { it.isQuoteLine() }

fun String.stripLeadingQuote(): String = lines()
    .dropWhile { it.isQuoteLine() }
    .joinToString("\n")
    .trim()

/** Wraps every line into a quote, nesting the quotes the text already carries. */
fun String.asQuote(): String = lines().joinToString("\n") { line ->
    // A blank line gets a bare marker: a trailing space would not survive the round trip
    if (line.isBlank()) QUOTE_MARKER else "$QUOTE_MARKER $line"
}

/**
 * Splits the text into lines tagged with their quote depth, dropping the markers.
 *
 * The space after a marker is optional, since translation and other round trips tend to eat it.
 * Blank lines are kept inside a quote and collapsed at its edges.
 */
fun parseQuoteLines(text: String): List<QuoteLine> {
    val lines = ArrayList<QuoteLine>()
    flatten(parseNodes(text.lines(), depth = 0), depth = 0, out = lines)
    return lines
}

private sealed interface QuoteNode {

    data class Line(val text: String) : QuoteNode

    data class Quote(val children: List<QuoteNode>) : QuoteNode
}

private fun parseNodes(lines: List<String>, depth: Int): List<QuoteNode> {
    val nodes = ArrayList<QuoteNode>()
    var i = 0
    while (i < lines.size) {
        // Past the nesting limit the markers stay in the text, keeping the recursion bounded
        if (lines[i].isQuoteLine() && depth < MAX_QUOTE_DEPTH) {
            val quoted = ArrayList<String>()
            while (i < lines.size && lines[i].isQuoteLine()) {
                quoted += lines[i].dropQuoteMarker()
                i++
            }
            nodes += QuoteNode.Quote(collapse(parseNodes(quoted, depth + 1)))
        } else {
            nodes += QuoteNode.Line(lines[i])
            i++
        }
    }
    return nodes
}

private fun collapse(nodes: List<QuoteNode>): List<QuoteNode> = nodes
    .filterNot { it is QuoteNode.Quote && it.children.isEmpty() }
    .dropWhile { it.isBlankLine() }
    .dropLastWhile { it.isBlankLine() }

private fun QuoteNode.isBlankLine(): Boolean = this is QuoteNode.Line && text.isBlank()

private fun String.dropQuoteMarker(): String {
    val rest = substring(QUOTE_MARKER.length)
    return if (rest.startsWith(' ')) rest.substring(1) else rest
}

private fun flatten(nodes: List<QuoteNode>, depth: Int, out: MutableList<QuoteLine>) {
    nodes.forEach { node ->
        when (node) {
            is QuoteNode.Line -> out += QuoteLine(depth, node.text)
            is QuoteNode.Quote -> flatten(node.children, depth + 1, out)
        }
    }
}

const val QUOTE_MARKER = ">"

private const val MAX_QUOTE_DEPTH = 6
