package com.tomclaw.appsend.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteParserTest {

    @Test
    fun `keeps plain text untouched`() {
        assertEquals(
            listOf(QuoteLine(0, "hello"), QuoteLine(0, ""), QuoteLine(0, "world")),
            parseQuoteLines("hello\n\nworld")
        )
    }

    @Test
    fun `treats an empty text as a single empty line`() {
        assertEquals(listOf(QuoteLine(0, "")), parseQuoteLines(""))
    }

    @Test
    fun `reads a quote with the usual marker and space`() {
        assertEquals(listOf(QuoteLine(1, "quoted")), parseQuoteLines("> quoted"))
    }

    @Test
    fun `reads a quote whose space the translation has eaten`() {
        assertEquals(listOf(QuoteLine(1, "quoted")), parseQuoteLines(">quoted"))
    }

    @Test
    fun `keeps a quote whole when only some lines kept the space`() {
        assertEquals(
            listOf(QuoteLine(1, "first"), QuoteLine(1, "second"), QuoteLine(1, "third")),
            parseQuoteLines("> first\n>second\n> third")
        )
    }

    @Test
    fun `keeps a blank line inside a quote`() {
        assertEquals(
            listOf(QuoteLine(1, "first"), QuoteLine(1, ""), QuoteLine(1, "second")),
            parseQuoteLines("> first\n>\n> second")
        )
    }

    @Test
    fun `keeps a blank line written with a trailing space`() {
        assertEquals(
            listOf(QuoteLine(1, "first"), QuoteLine(1, ""), QuoteLine(1, "second")),
            parseQuoteLines("> first\n> \n> second")
        )
    }

    @Test
    fun `collapses a blank line at the end of a quote`() {
        assertEquals(
            listOf(QuoteLine(1, "quoted"), QuoteLine(0, "answer")),
            parseQuoteLines("> quoted\n>\nanswer")
        )
    }

    @Test
    fun `collapses a blank line at the start of a quote`() {
        assertEquals(listOf(QuoteLine(1, "quoted")), parseQuoteLines(">\n> quoted"))
    }

    @Test
    fun `collapses a quote made of blank lines alone`() {
        assertEquals(listOf(QuoteLine(0, "answer")), parseQuoteLines(">\n>\nanswer"))
    }

    @Test
    fun `reads a nested quote`() {
        assertEquals(
            listOf(QuoteLine(2, "grandparent"), QuoteLine(1, "parent"), QuoteLine(0, "answer")),
            parseQuoteLines("> > grandparent\n> parent\nanswer")
        )
    }

    @Test
    fun `reads a nested quote written without spaces`() {
        assertEquals(
            listOf(QuoteLine(2, "grandparent"), QuoteLine(1, "parent")),
            parseQuoteLines(">>grandparent\n>parent")
        )
    }

    @Test
    fun `collapses a blank line at the end of a nested quote`() {
        assertEquals(
            listOf(QuoteLine(2, "grandparent"), QuoteLine(1, "parent")),
            parseQuoteLines("> > grandparent\n> >\n> parent")
        )
    }

    @Test
    fun `stops nesting at the depth limit and keeps the rest as text`() {
        assertEquals(
            listOf(QuoteLine(6, ">> deep")),
            parseQuoteLines(">".repeat(8) + " deep")
        )
    }

    @Test
    fun `survives an absurdly deep nesting`() {
        val text = ">".repeat(20_000) + " deep"

        assertEquals(listOf(QuoteLine(6, ">".repeat(20_000 - 6) + " deep")), parseQuoteLines(text))
    }

    @Test
    fun `separates quotes split by plain text`() {
        assertEquals(
            listOf(
                QuoteLine(1, "first"),
                QuoteLine(0, "between"),
                QuoteLine(1, "second"),
            ),
            parseQuoteLines("> first\nbetween\n> second")
        )
    }

    @Test
    fun `reads a text with windows line breaks`() {
        assertEquals(
            listOf(QuoteLine(1, "quoted"), QuoteLine(0, "answer")),
            parseQuoteLines("> quoted\r\nanswer")
        )
    }

    @Test
    fun `keeps a marker that is not at the line start`() {
        assertEquals(listOf(QuoteLine(0, "a > b")), parseQuoteLines("a > b"))
    }

    @Test
    fun `keeps the text of a quoted line intact`() {
        assertEquals(listOf(QuoteLine(1, "  indented")), parseQuoteLines(">   indented"))
    }

    @Test
    fun `tells a quote line from a plain one`() {
        assertTrue("> quoted".isQuoteLine())
        assertTrue(">quoted".isQuoteLine())
        assertTrue(">".isQuoteLine())
        assertFalse("quoted".isQuoteLine())
        assertFalse(" > quoted".isQuoteLine())
    }

    @Test
    fun `tells a text carrying a quote from one without`() {
        assertTrue("> quoted".hasQuote())
        assertTrue("answer\n>quoted".hasQuote())
        assertTrue(">".hasQuote())
        assertFalse("no quote here".hasQuote())
        assertFalse("a > b".hasQuote())
        assertFalse("".hasQuote())
    }

    @Test
    fun `strips the leading quote`() {
        assertEquals("answer", "> quoted\nanswer".stripLeadingQuote())
    }

    @Test
    fun `strips a leading quote whose blank line lost its space`() {
        assertEquals("answer", "> quoted\n>\n> more\nanswer".stripLeadingQuote())
    }

    @Test
    fun `strips a leading quote whose space the translation has eaten`() {
        assertEquals("answer", ">quoted\nanswer".stripLeadingQuote())
    }

    @Test
    fun `strips a leading nested quote`() {
        assertEquals("answer", "> > grandparent\n> parent\nanswer".stripLeadingQuote())
    }

    @Test
    fun `leaves a trailing quote in place while stripping`() {
        assertEquals("answer\n> quoted", "answer\n> quoted".stripLeadingQuote())
    }

    @Test
    fun `quotes a text line by line`() {
        assertEquals("> first\n> second", "first\nsecond".asQuote())
    }

    @Test
    fun `quotes a blank line without a trailing space`() {
        assertEquals("> first\n>\n> second", "first\n\nsecond".asQuote())
    }

    @Test
    fun `nests the quotes the text already carries`() {
        assertEquals("> answer\n> > quoted", "answer\n> quoted".asQuote())
    }

    @Test
    fun `reads back the quote it has written`() {
        val text = "answer\n\n> quoted"
        assertEquals(
            listOf(QuoteLine(1, "answer"), QuoteLine(1, ""), QuoteLine(2, "quoted")),
            parseQuoteLines(text.asQuote())
        )
    }
}
