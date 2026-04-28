package com.tomclaw.appsend.screen.details.adapter.user_rate

import android.view.View
import android.widget.RatingBar
import com.tomclaw.appsend.R
import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.uikit.permissions.PermissionBanner
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView

interface UserRateItemView : ItemView {

    fun setRating(value: Float)

    fun setOnRateListener(listener: ((Float) -> Unit)?)

    /** Lock or unlock interactive rating-bar input. */
    fun setRatingEditable(editable: Boolean)

    /** Enable/disable the feedback (rate) button. */
    fun setFeedbackEnabled(enabled: Boolean)

    /**
     * Show the rate-permission banner for the given denied capability
     * or hide it when `null`. Composition of "denied vs allowed" lives
     * in the item presenter; this setter just renders.
     */
    fun setDenialBanner(capability: Capability?)

}

class UserRateItemViewHolder(view: View) : BaseItemViewHolder(view), UserRateItemView {

    private val ratingView: RatingBar = view.findViewById(R.id.rating_view)
    private val feedbackButton: View = view.findViewById(R.id.feedback_button)
    private val banner: PermissionBanner = view.findViewById(R.id.rate_permission_banner)

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

    override fun setRatingEditable(editable: Boolean) {
        ratingView.setIsIndicator(!editable)
    }

    override fun setFeedbackEnabled(enabled: Boolean) {
        feedbackButton.isEnabled = enabled
    }

    override fun setDenialBanner(capability: Capability?) {
        if (capability == null) {
            banner.hide()
        } else {
            banner.showFor(capability)
        }
    }

    override fun onUnbind() {
        this.rateListener = null
    }

}
