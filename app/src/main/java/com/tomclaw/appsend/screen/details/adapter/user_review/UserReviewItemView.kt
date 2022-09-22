package com.tomclaw.appsend.screen.details.adapter.user_review

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

interface UserReviewItemView : ItemView {

    fun setMemberName(name: String)

    fun setMemberIcon(userIcon: UserIcon)

    fun setRating(value: Float)

    fun setDate(date: String)

    fun setReview(text: String?)

    fun setOnEditListener(listener: (() -> Unit)?)

}

class UserReviewItemViewHolder(view: View) : BaseViewHolder(view), UserReviewItemView {

    private val memberIcon: UserIconView = UserIconViewImpl(view.findViewById(R.id.member_icon))
    private val memberName: TextView = view.findViewById(R.id.member_name)
    private val ratingView: RatingBar = view.findViewById(R.id.rating_view)
    private val dateView: TextView = view.findViewById(R.id.date_view)
    private val reviewView: TextView = view.findViewById(R.id.review_view)
    private val feedbackButton: View = view.findViewById(R.id.feedback_button)

    private var editListener: (() -> Unit)? = null

    init {
        feedbackButton.setOnClickListener { editListener?.invoke() }
        ratingView.setOnRatingBarChangeListener { _, _, fromUser ->
            if (fromUser) {
                editListener?.invoke()
            }
        }
    }

    override fun setMemberIcon(userIcon: UserIcon) {
        this.memberIcon.bind(userIcon)
    }

    override fun setMemberName(name: String) {
        this.memberName.bind(name)
    }

    override fun setRating(value: Float) {
        ratingView.rating = value
    }

    override fun setDate(date: String) {
        dateView.bind(date)
    }

    override fun setReview(text: String?) {
        reviewView.bind(text)
    }

    override fun setOnEditListener(listener: (() -> Unit)?) {
        this.editListener = listener
    }

    override fun onUnbind() {
        this.editListener = null
    }

}
