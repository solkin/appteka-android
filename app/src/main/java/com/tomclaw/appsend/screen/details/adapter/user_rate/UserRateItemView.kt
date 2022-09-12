package com.tomclaw.appsend.screen.details.adapter.user_rate

import android.view.View
import android.widget.RatingBar
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R

interface UserRateItemView : ItemView {

    fun setRating(value: Float)

    fun setOnClickListener(listener: (() -> Unit)?)

}

class UserRateItemViewHolder(view: View) : BaseViewHolder(view), UserRateItemView {

    private val ratingView: RatingBar = view.findViewById(R.id.rating_view)

    private var clickListener: (() -> Unit)? = null

    init {
        ratingView.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            clickListener?.invoke()
        }
    }

    override fun setRating(value: Float) {
        ratingView.rating = value
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
