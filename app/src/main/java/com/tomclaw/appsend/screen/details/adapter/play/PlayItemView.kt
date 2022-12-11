package com.tomclaw.appsend.screen.details.adapter.play

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGImageView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.getAttributedColor
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.util.svgToDrawable

interface PlayItemView : ItemView {

    fun showRating(rating: String)

    fun hideRating()

    fun setDownloads(downloads: Int)

    fun setSize(size: String)

    fun showExclusive()

    fun hideExclusive()

    fun showCategory(icon: String, title: String)

    fun hideCategory()

    fun showOsVersionCompatible(version: String)

    fun showOsVersionIncompatible(version: String)

    fun hideOsVersion()

}

class PlayItemViewHolder(view: View) : BaseViewHolder(view), PlayItemView {

    private val context = view.context
    private val ratingContainer: View = view.findViewById(R.id.rating_container)
    private val ratingView: TextView = view.findViewById(R.id.rating_view)
    private val downloadsView: TextView = view.findViewById(R.id.downloads_view)
    private val sizeView: TextView = view.findViewById(R.id.size_view)
    private val exclusiveContainer: View = view.findViewById(R.id.exclusive_container)
    private val categoryContainer: View = view.findViewById(R.id.category_container)
    private val categorySvg: ImageView = view.findViewById(R.id.category_svg)
    private val categoryTitle: TextView = view.findViewById(R.id.category_title)
    private val osVersionContainer: View = view.findViewById(R.id.os_version_container)
    private val osVersionView: TextView = view.findViewById(R.id.os_version_view)
    private val osIncompatibleImage: ImageView = view.findViewById(R.id.os_incompatible_image)

    override fun showRating(rating: String) {
        ratingContainer.show()
        ratingView.bind(rating)
    }

    override fun hideRating() {
        ratingContainer.hide()
    }

    override fun setDownloads(downloads: Int) {
        downloadsView.bind(downloads.toString())
    }

    override fun setSize(size: String) {
        sizeView.bind(size)
    }

    override fun showExclusive() {
        exclusiveContainer.show()
    }

    override fun hideExclusive() {
        exclusiveContainer.hide()
    }

    override fun showCategory(icon: String, title: String) {
        categoryContainer.show()
        categorySvg.setImageDrawable(svgToDrawable(icon, context.resources))
        categoryTitle.bind(title)
    }

    override fun hideCategory() {
        categoryContainer.hide()
    }

    override fun showOsVersionCompatible(version: String) {
        osVersionContainer.show()
        osVersionView.bind(version)
        osVersionView.setTextColor(getAttributedColor(context, R.attr.text_primary_color))
        osIncompatibleImage.hide()
    }

    override fun showOsVersionIncompatible(version: String) {
        osVersionContainer.show()
        osVersionView.bind(version)
        osVersionView.setTextColor(context.resources.getColor(R.color.sdk_incompatible_tint))
        osIncompatibleImage.show()
    }

    override fun hideOsVersion() {
        osVersionContainer.hide()
    }

}
