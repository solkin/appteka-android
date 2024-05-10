package com.tomclaw.appsend.screen.ratings.adapter.rating

import android.view.View
import android.widget.ListAdapter
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.MenuRes
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.main.adapter.MenuAdapter
import com.tomclaw.appsend.screen.ratings.RatingsPreferencesProvider
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.getAttributedColor
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl

interface RatingItemView : ItemView {

    fun setUserIcon(userIcon: UserIcon)

    fun setUserName(name: String)

    fun setRating(value: Float)

    fun setDate(date: String)

    fun setComment(text: String?)

    fun setOnRatingClickListener(listener: (() -> Unit)?)

    fun setOnDeleteClickListener(listener: (() -> Unit)?)

}

class RatingItemViewHolder(
    private val view: View,
    private val preferences: RatingsPreferencesProvider,
) : BaseViewHolder(view), RatingItemView {

    private val userIconView: UserIconView = UserIconViewImpl(view.findViewById(R.id.member_icon))
    private val userNameView: TextView = view.findViewById(R.id.user_name)
    private val ratingView: RatingBar = view.findViewById(R.id.rating_view)
    private val dateView: TextView = view.findViewById(R.id.date_view)
    private val commentView: TextView = view.findViewById(R.id.comment_view)
    private val menuView: View = view.findViewById(R.id.rating_menu)

    private var ratingClickListener: (() -> Unit)? = null
    private var deleteClickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { ratingClickListener?.invoke() }
        menuView.setOnClickListener {
            showRatingDialog()
        }
    }

    override fun setUserIcon(userIcon: UserIcon) {
        userIconView.bind(userIcon)
    }

    override fun setUserName(name: String) {
        userNameView.bind(name)
    }

    override fun setRating(value: Float) {
        ratingView.rating = value
    }

    override fun setDate(date: String) {
        dateView.bind(date)
    }

    override fun setComment(text: String?) {
        commentView.bind(text)
    }

    private fun showRatingDialog() {
        showRatingDialog(R.menu.rating_menu)
    }

    private fun showRatingDialog(@MenuRes menuId: Int) {
        val theme = R.style.BottomSheetDialogDark.takeIf { preferences.isDarkTheme() }
            ?: R.style.BottomSheetDialogLight
        BottomSheetBuilder(view.context, theme)
            .setMode(BottomSheetBuilder.MODE_LIST)
            .setIconTintColor(getAttributedColor(view.context, R.attr.menu_icons_tint))
            .setItemTextColor(getAttributedColor(view.context, R.attr.text_primary_color))
            .setMenu(menuId)
            .setItemClickListener {
                when (it.itemId) {
                    R.id.menu_profile -> ratingClickListener?.invoke()
                    R.id.menu_delete -> deleteClickListener?.invoke()
                }
            }
            .createDialog()
            .show()
    }

    override fun setOnRatingClickListener(listener: (() -> Unit)?) {
        this.ratingClickListener = listener
    }

    override fun setOnDeleteClickListener(listener: (() -> Unit)?) {
        this.deleteClickListener = listener
    }

    override fun onUnbind() {
        this.ratingClickListener = null
        this.deleteClickListener = null
    }

}
