package com.tomclaw.appsend.screen.details.adapter.user_rate

import android.view.View
import android.widget.RatingBar
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R

interface UserRateItemView : ItemView {

    fun setRating(value: Float)

    fun setOnRateListener(listener: ((Float) -> Unit)?)
}

class UserRateItemViewHolder(view: View) : BaseViewHolder(view), UserRateItemView {

    private val ratingView: RatingBar = view.findViewById(R.id.rating_view)
    private val feedbackButton: View = view.findViewById(R.id.feedback_button)

    private var rateListener: ((Float) -> Unit)? = null

    init {
        feedbackButton.setOnClickListener { rateListener?.invoke(ratingView.rating) }
        ratingView.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                rateListener?.invoke(rating)
            }
        }
    }

    override fun setRating(value: Float) {
        ratingView.rating = value
    }

    override fun setOnRateListener(listener: ((Float) -> Unit)?) {
        this.rateListener = listener
    }

    override fun onUnbind() {
        this.rateListener = null
    }

}
