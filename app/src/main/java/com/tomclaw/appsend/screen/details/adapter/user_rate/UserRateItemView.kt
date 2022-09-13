package com.tomclaw.appsend.screen.details.adapter.user_rate

import android.view.View
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import me.zhanghai.android.materialratingbar.MaterialRatingBar

interface UserRateItemView : ItemView {

    fun setRating(value: Float)

    fun setOnClickListener(listener: (() -> Unit)?)

}

class UserRateItemViewHolder(view: View) : BaseViewHolder(view), UserRateItemView {

    private val ratingView: MaterialRatingBar = view.findViewById(R.id.rating_view)
    private val feedbackButton: View = view.findViewById(R.id.feedback_button)

    private var clickListener: (() -> Unit)? = null

    init {
        feedbackButton.setOnClickListener { clickListener?.invoke() }
        ratingView.setOnRatingBarChangeListener { _, _, fromUser ->
            if (fromUser) {
                clickListener?.invoke()
            }
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
