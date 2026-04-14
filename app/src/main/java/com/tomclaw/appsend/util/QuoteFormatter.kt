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
    if (!text.startsWith("> ") && "\n> " !in text) {
        return text
    }

    val typedValue = TypedValue()
    context.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
    val stripeColor = typedValue.data
    val density = context.resources.displayMetrics.density
    val stripeWidth = density * STRIPE_WIDTH_DP
    val gapWidth = (density * GAP_WIDTH_DP).toInt()
    val paddingV = (density * PADDING_V_DP).toInt()
    val bottomGap = (density * BOTTOM_GAP_DP).toInt()

    val lines = text.split('\n')
    val builder = SpannableStringBuilder()

    var i = 0
    while (i < lines.size) {
        if (lines[i].startsWith("> ")) {
            if (builder.isNotEmpty()) builder.append('\n')
            val quoteStart = builder.length
            while (i < lines.size && lines[i].startsWith("> ")) {
                if (builder.length > quoteStart) builder.append('\n')
                builder.append(lines[i].removePrefix("> "))
                i++
            }
            val quoteEnd = builder.length
            val hasTextAfter = i < lines.size
            builder.setSpan(
                QuoteStripeSpan(stripeColor, stripeWidth, gapWidth, paddingV, if (hasTextAfter) bottomGap else 0),
                quoteStart, quoteEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builder.setSpan(
                StyleSpan(Typeface.ITALIC),
                quoteStart, quoteEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            if (builder.isNotEmpty()) builder.append('\n')
            builder.append(lines[i])
            i++
        }
    }

    return builder
}

private class QuoteStripeSpan(
    private val stripeColor: Int,
    private val stripeWidth: Float,
    private val gapWidth: Int,
    private val paddingVertical: Int,
    private val bottomGap: Int,
) : LeadingMarginSpan, LineBackgroundSpan, LineHeightSpan {

    private val rect = RectF()

    override fun getLeadingMargin(first: Boolean): Int {
        return stripeWidth.toInt() + gapWidth
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

        val drawTop = if (isFirstLine) (top - paddingVertical).toFloat() else top.toFloat()
        val drawBottom = if (isLastLine) (bottom - bottomGap + paddingVertical).toFloat() else bottom.toFloat()

        val savedColor = paint.color
        val savedStyle = paint.style
        paint.color = stripeColor
        paint.style = Paint.Style.FILL

        val stripeLeft = left.toFloat()
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
        if (bottomGap == 0) return
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
