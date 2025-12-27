package com.tomclaw.appsend.util.bdui.model

import com.google.gson.annotations.SerializedName

/**
 * Text styling options for text-based components.
 */
data class BduiTextStyle(
    @SerializedName("textSize")
    val textSize: Int? = null,              // in sp
    @SerializedName("textColor")
    val textColor: String? = null,          // "#RRGGBB" or "#AARRGGBB"
    @SerializedName("fontWeight")
    val fontWeight: String? = null,         // "normal", "bold", "medium", "light"
    @SerializedName("fontFamily")
    val fontFamily: String? = null,         // "sans-serif", "serif", "monospace"
    @SerializedName("textAlign")
    val textAlign: String? = null,          // "start", "center", "end"
    @SerializedName("maxLines")
    val maxLines: Int? = null,
    @SerializedName("ellipsize")
    val ellipsize: String? = null,          // "start", "middle", "end", "marquee"
    @SerializedName("lineHeight")
    val lineHeight: Int? = null,            // in sp
    @SerializedName("letterSpacing")
    val letterSpacing: Float? = null,
    @SerializedName("textAllCaps")
    val textAllCaps: Boolean? = null,
    @SerializedName("includeFontPadding")
    val includeFontPadding: Boolean? = null
)

/**
 * Background styling options.
 */
data class BduiBackgroundStyle(
    @SerializedName("color")
    val color: String? = null,              // "#RRGGBB" or "#AARRGGBB"
    @SerializedName("cornerRadius")
    val cornerRadius: Int? = null,          // in dp, applies to all corners
    @SerializedName("cornerRadiusTopStart")
    val cornerRadiusTopStart: Int? = null,
    @SerializedName("cornerRadiusTopEnd")
    val cornerRadiusTopEnd: Int? = null,
    @SerializedName("cornerRadiusBottomStart")
    val cornerRadiusBottomStart: Int? = null,
    @SerializedName("cornerRadiusBottomEnd")
    val cornerRadiusBottomEnd: Int? = null,
    @SerializedName("strokeColor")
    val strokeColor: String? = null,
    @SerializedName("strokeWidth")
    val strokeWidth: Int? = null            // in dp
)

/**
 * Image styling options.
 */
data class BduiImageStyle(
    @SerializedName("scaleType")
    val scaleType: String? = null,          // "centerCrop", "fitCenter", "centerInside", etc.
    @SerializedName("tint")
    val tint: String? = null,               // Tint color
    @SerializedName("cornerRadius")
    val cornerRadius: Int? = null           // in dp
)

