package com.tomclaw.appsend.screen.rate

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.clicks
import com.tomclaw.appsend.util.disable
import com.tomclaw.appsend.util.enable
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.showWithAlphaAnimation
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl
import com.tomclaw.imageloader.util.centerCrop
import com.tomclaw.imageloader.util.fetch
import com.tomclaw.imageloader.util.withPlaceholder
import io.reactivex.rxjava3.core.Observable

interface RateView {

    fun setTitle(title: String)

    fun setIcon(url: String?)

    fun setMemberIcon(userIcon: UserIcon)

    fun setMemberName(name: String)

    fun setRating(rating: Float)

    fun setReview(review: String)

    fun enableSubmitButton()

    fun disableSubmitButton()

    fun showProgress()

    fun showContent()

    fun showError()

    fun navigationClicks(): Observable<Unit>

    fun ratingChanged(): Observable<Float>

    fun reviewEditChanged(): Observable<String>

    fun submitClicks(): Observable<Unit>

}

class RateViewImpl(view: View) : RateView {

    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)
    private val scrollView: View = view.findViewById(R.id.scroll_view)
    private val back: View = view.findViewById(R.id.go_back)
    private val icon: ImageView = view.findViewById(R.id.icon)
    private val title: TextView = view.findViewById(R.id.title)
    private val subtitle: TextView = view.findViewById(R.id.subtitle)
    private val memberIcon: UserIconView = UserIconViewImpl(view.findViewById(R.id.member_icon))
    private val memberName: TextView = view.findViewById(R.id.member_name)
    private val ratingView: RatingBar = view.findViewById(R.id.rating_view)
    private val reviewEdit: EditText = view.findViewById(R.id.review_edit)
    private val submitButton: View = view.findViewById(R.id.submit_button)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val ratingRelay = PublishRelay.create<Float>()
    private val reviewEditRelay = PublishRelay.create<String>()
    private val submitRelay = PublishRelay.create<Unit>()

    init {
        subtitle.setText(R.string.rate_app)
        back.clicks(navigationRelay)
        ratingView.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                ratingRelay.accept(rating)
            }
        }
        reviewEdit.addTextChangedListener { text ->
            reviewEditRelay.accept(text.toString())
        }
        submitButton.clicks(submitRelay)
    }

    override fun setTitle(title: String) {
        this.title.bind(title)
    }

    override fun setIcon(url: String?) {
        icon.fetch(url.orEmpty()) {
            centerCrop()
            withPlaceholder(R.drawable.app_placeholder)
            placeholder = {
                with(it.get()) {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageResource(R.drawable.app_placeholder)
                }
            }
        }
    }

    override fun setMemberIcon(userIcon: UserIcon) {
        this.memberIcon.bind(userIcon)
    }

    override fun setMemberName(name: String) {
        this.memberName.bind(name)
    }

    override fun setRating(rating: Float) {
        this.ratingView.rating = rating
    }

    override fun setReview(review: String) {
        this.reviewEdit.setText(review, TextView.BufferType.EDITABLE)
    }

    override fun enableSubmitButton() {
        submitButton.enable()
    }

    override fun disableSubmitButton() {
        submitButton.disable()
    }

    override fun showProgress() {
        overlayProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun showError() {
        Snackbar.make(scrollView, R.string.review_publish_failed, Snackbar.LENGTH_SHORT).show()
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun ratingChanged(): Observable<Float> = ratingRelay

    override fun reviewEditChanged(): Observable<String> = reviewEditRelay

    override fun submitClicks(): Observable<Unit> = submitRelay

}
