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

    /**
     * Apply the server-resolved "app.rate" capability: enabled → rating
     * bar interactive and banner hidden, denied → bar locked and banner
     * explains why.
     */
    fun setRateCapability(capability: Capability?)

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

    override fun setRateCapability(capability: Capability?) {
        val denied = capability != null && !capability.allowed
        ratingView.setIsIndicator(denied)
        feedbackButton.isEnabled = !denied
        if (denied) {
            banner.showFor(capability)
        } else {
            banner.hide()
        }
    }

    override fun onUnbind() {
        this.rateListener = null
    }

}
