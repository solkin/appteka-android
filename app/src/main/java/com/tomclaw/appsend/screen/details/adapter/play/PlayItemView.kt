package com.tomclaw.appsend.screen.details.adapter.play

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
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

    fun showFavorites(favorites: Int)

    fun hideFavorites()

    fun setSize(size: String)

    fun showExclusive()

    fun hideExclusive()

    fun showOpenSource()

    fun hideOpenSource()

    fun showCategory(icon: String, title: String)

    fun hideCategory()

    fun showOsVersionCompatible(version: String)

    fun showOsVersionIncompatible(version: String)

    fun hideOsVersion()

    fun showSecurityScanning(title: String)

    fun showSecuritySafe(title: String)

    fun showSecuritySuspicious(title: String)

    fun showSecurityMalware(title: String)

    fun showSecurityNotChecked(title: String)

    fun hideSecurityStatus()

    fun setOnSecurityClickListener(listener: (() -> Unit)?)

}

class PlayItemViewHolder(view: View) : BaseViewHolder(view), PlayItemView {

    private val context = view.context
    private val ratingContainer: View = view.findViewById(R.id.rating_container)
    private val ratingView: TextView = view.findViewById(R.id.rating_view)
    private val downloadsView: TextView = view.findViewById(R.id.downloads_view)
    private val favoritesContainer: View = view.findViewById(R.id.favorites_container)
    private val favoritesView: TextView = view.findViewById(R.id.favorites_view)
    private val sizeView: TextView = view.findViewById(R.id.size_view)
    private val exclusiveContainer: View = view.findViewById(R.id.exclusive_container)
    private val openSourceContainer: View = view.findViewById(R.id.open_source_container)
    private val categoryContainer: View = view.findViewById(R.id.category_container)
    private val categorySvg: ImageView = view.findViewById(R.id.category_svg)
    private val categoryTitle: TextView = view.findViewById(R.id.category_title)
    private val osVersionContainer: View = view.findViewById(R.id.os_version_container)
    private val osVersionView: TextView = view.findViewById(R.id.os_version_view)
    private val osIncompatibleImage: ImageView = view.findViewById(R.id.os_incompatible_image)
    private val securityContainer: View = view.findViewById(R.id.security_container)
    private val securityClickable: View = view.findViewById(R.id.security_clickable)
    private val securityIcon: ImageView = view.findViewById(R.id.security_icon)
    private val securityTitle: TextView = view.findViewById(R.id.security_title)

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

    override fun showFavorites(favorites: Int) {
        favoritesContainer.show()
        favoritesView.bind(favorites.toString())
    }

    override fun hideFavorites() {
        favoritesContainer.hide()
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

    override fun showOpenSource() {
        openSourceContainer.show()
    }

    override fun hideOpenSource() {
        openSourceContainer.hide()
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
        osVersionView.setTextColor(ContextCompat.getColor(context, R.color.sdk_incompatible_tint))
        osIncompatibleImage.show()
    }

    override fun hideOsVersion() {
        osVersionContainer.hide()
    }

    override fun showSecurityScanning(title: String) {
        securityContainer.show()
        securityIcon.setImageResource(R.drawable.ic_timer_sand)
        securityIcon.setColorFilter(ContextCompat.getColor(context, R.color.block_info_color))
        securityTitle.bind(title)
    }

    override fun showSecuritySafe(title: String) {
        securityContainer.show()
        securityIcon.setImageResource(R.drawable.ic_verified)
        securityIcon.setColorFilter(ContextCompat.getColor(context, R.color.block_success_color))
        securityTitle.bind(title)
    }

    override fun showSecuritySuspicious(title: String) {
        securityContainer.show()
        securityIcon.setImageResource(R.drawable.ic_warning)
        securityIcon.setColorFilter(ContextCompat.getColor(context, R.color.block_warning_color))
        securityTitle.bind(title)
    }

    override fun showSecurityMalware(title: String) {
        securityContainer.show()
        securityIcon.setImageResource(R.drawable.ic_virus)
        securityIcon.setColorFilter(ContextCompat.getColor(context, R.color.block_error_color))
        securityTitle.bind(title)
    }

    override fun showSecurityNotChecked(title: String) {
        securityContainer.show()
        securityIcon.setImageResource(R.drawable.ic_security)
        securityIcon.setColorFilter(ContextCompat.getColor(context, R.color.block_warning_color))
        securityTitle.bind(title)
    }

    override fun hideSecurityStatus() {
        securityContainer.hide()
    }

    override fun setOnSecurityClickListener(listener: (() -> Unit)?) {
        if (listener != null) {
            securityClickable.isClickable = true
            securityClickable.setOnClickListener { listener.invoke() }
        } else {
            securityClickable.isClickable = false
            securityClickable.setOnClickListener(null)
        }
    }

}
