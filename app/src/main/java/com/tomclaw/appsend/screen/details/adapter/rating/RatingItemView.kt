package com.tomclaw.appsend.screen.details.adapter.rating

import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl

interface RatingItemView : ItemView {

    fun setUserIcon(userIcon: UserIcon)

    fun setRating(value: Float)

    fun setDate(date: String)

    fun setComment(text: String?)

    fun setOnClickListener(listener: (() -> Unit)?)

}

class RatingItemViewHolder(view: View) : BaseViewHolder(view), RatingItemView {

    private val context = view.context
    private val userIconView: UserIconView = UserIconViewImpl(view.findViewById(R.id.member_icon))
    private val ratingView: RatingBar = view.findViewById(R.id.rating_view)
    private val dateView: TextView = view.findViewById(R.id.date_view)
    private val commentView: TextView = view.findViewById(R.id.comment_view)

    private var clickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { clickListener?.invoke() }
    }

    override fun setUserIcon(userIcon: UserIcon) {
        userIconView.bind(userIcon)
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

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
