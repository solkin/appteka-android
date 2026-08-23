package com.tomclaw.appsend.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.text.style.LineBackgroundSpan
import android.text.style.LineHeightSpan
import android.text.style.StyleSpan
import android.util.TypedValue

fun formatMessageText(text: String, context: Context): CharSequence {
    if (!text.hasQuote()) {
        return text
    }
    val lines = parseQuoteLines(text)
    val maxDepth = lines.maxOfOrNull { it.depth } ?: 0

    val typedValue = TypedValue()
    context.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
    val stripeColor = typedValue.data
    val density = context.resources.displayMetrics.density
    val stripeWidth = density * STRIPE_WIDTH_DP
    val gapWidth = (density * GAP_WIDTH_DP).toInt()
    val paddingV = (density * PADDING_V_DP).toInt()
    val bottomGap = (density * BOTTOM_GAP_DP).toInt()

    val builder = SpannableStringBuilder()
    val starts = IntArray(lines.size)
    val ends = IntArray(lines.size)
    lines.forEachIndexed { index, line ->
        if (index > 0) builder.append('\n')
        starts[index] = builder.length
        builder.append(line.text)
        ends[index] = builder.length
    }

    for (depth in 1..maxDepth) {
        forEachQuoteRun(lines, depth) { from, to ->
            // A nested run takes its padding and gap from the quote it sits in, not from itself
            val quoteStarts = from == 0 || lines[from - 1].depth == 0
            val quoteEnds = to == lines.lastIndex || lines[to + 1].depth == 0
            builder.setSpan(
                QuoteStripeSpan(
                    stripeColor = stripeColor,
                    stripeWidth = stripeWidth,
                    gapWidth = gapWidth,
                    paddingTop = if (quoteStarts) paddingV else 0,
                    paddingBottom = if (quoteEnds) paddingV else 0,
                    bottomGap = if (quoteEnds && to < lines.lastIndex) bottomGap else 0,
                    outermost = depth == 1,
                    depth = depth,
                ),
                starts[from], ends[to], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (depth == 1) {
                builder.setSpan(
                    StyleSpan(Typeface.ITALIC),
                    starts[from], ends[to], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    return builder
}

/** Calls back with the bounds of every maximal run of lines quoted at least [depth] times. */
private inline fun forEachQuoteRun(
    lines: List<QuoteLine>,
    depth: Int,
    action: (from: Int, to: Int) -> Unit,
) {
    var i = 0
    while (i < lines.size) {
        if (lines[i].depth < depth) {
            i++
            continue
        }
        val from = i
        while (i < lines.size && lines[i].depth >= depth) i++
        action(from, i - 1)
    }
}

private class QuoteStripeSpan(
    private val stripeColor: Int,
    private val stripeWidth: Float,
    private val gapWidth: Int,
    private val paddingTop: Int,
    private val paddingBottom: Int,
    private val bottomGap: Int,
    private val outermost: Boolean,
    private val depth: Int,
) : LeadingMarginSpan, LineBackgroundSpan, LineHeightSpan {

    private val rect = RectF()

    private val step = stripeWidth.toInt() + gapWidth

    override fun getLeadingMargin(first: Boolean): Int {
        return step
    }

    override fun drawLeadingMargin(
        c: Canvas, p: Paint, x: Int, dir: Int,
        top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int,
        first: Boolean, layout: Layout
    ) {
    }

    override fun drawBackground(
        canvas: Canvas, paint: Paint,
        left: Int, right: Int,
        top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int,
        lineNumber: Int
    ) {
        val spanned = text as Spanned
        val spanStart = spanned.getSpanStart(this)
        val spanEnd = spanned.getSpanEnd(this)
        val isFirstLine = start <= spanStart
        val isLastLine = end >= spanEnd

        val drawTop = if (isFirstLine) (top - paddingTop).toFloat() else top.toFloat()
        val drawBottom = if (isLastLine) {
            (bottom - bottomGap + paddingBottom).toFloat()
        } else {
            bottom.toFloat()
        }

        val savedColor = paint.color
        val savedStyle = paint.style
        paint.color = stripeColor
        paint.style = Paint.Style.FILL

        val stripeLeft = (left + (depth - 1) * step).toFloat()
        rect.set(stripeLeft, drawTop, stripeLeft + stripeWidth, drawBottom)
        canvas.drawRect(rect, paint)

        paint.color = savedColor
        paint.style = savedStyle
    }

    override fun chooseHeight(
        text: CharSequence, start: Int, end: Int,
        spanstartv: Int, lineHeight: Int,
        fm: Paint.FontMetricsInt
    ) {
        if (!outermost || bottomGap == 0) return
        val spanned = text as Spanned
        val spanEnd = spanned.getSpanEnd(this)
        if (end >= spanEnd) {
            fm.descent += bottomGap
            fm.bottom += bottomGap
        }
    }
}

private const val STRIPE_WIDTH_DP = 3f
private const val GAP_WIDTH_DP = 8f
private const val PADDING_V_DP = 2f
private const val BOTTOM_GAP_DP = 8f
