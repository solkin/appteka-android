package com.tomclaw.appsend.screen.ratings.adapter.rating

import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.main.adapter.files.ActionItem
import com.tomclaw.appsend.main.adapter.files.ActionsAdapter
import com.tomclaw.appsend.screen.ratings.RatingsPreferencesProvider
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl

interface RatingItemView : ItemView {
    fun setUserIcon(userIcon: UserIcon)
    fun setUserName(name: String)
    fun setRating(value: Float)
    fun setDate(date: String)
    fun setComment(text: String?)
    fun showRatingMenu()
    fun hideRatingMenu()
    fun setOnRatingClickListener(listener: (() -> Unit)?)
    fun setOnDeleteClickListener(listener: (() -> Unit)?)
}

class RatingItemViewHolder(
    private val view: View,
    private val preferences: RatingsPreferencesProvider,
) : BaseViewHolder(view), RatingItemView {

    private val context = view.context
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

    override fun showRatingMenu() = menuView.show()

    override fun hideRatingMenu() = menuView.hide()

    override fun setOnRatingClickListener(listener: (() -> Unit)?) {
        this.ratingClickListener = listener
    }

    override fun setOnDeleteClickListener(listener: (() -> Unit)?) {
        this.deleteClickListener = listener
    }

    // Material 3 Bottom Sheet using same pattern as ChatViewImpl
    private fun showRatingDialog() {
        val bottomSheetDialog = BottomSheetDialog(context)
        val sheetView = View.inflate(context, R.layout.bottom_sheet_actions, null)
        val actionsRecycler: RecyclerView = sheetView.findViewById(R.id.actions_recycler)

        val actions = listOf(
            ActionItem(R.id.menu_profile, context.getString(R.string.profile), R.drawable.ic_account),
            ActionItem(R.id.menu_delete, context.getString(R.string.delete), R.drawable.ic_delete)
        )

        val adapter = ActionsAdapter(actions) { clickedId ->
            bottomSheetDialog.dismiss()
            when (clickedId) {
                R.id.menu_profile -> ratingClickListener?.invoke()
                R.id.menu_delete -> deleteClickListener?.invoke()
            }
        }

        actionsRecycler.layoutManager = LinearLayoutManager(context)
        actionsRecycler.adapter = adapter

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
    }

    override fun onUnbind() {
        ratingClickListener = null
        deleteClickListener = null
    }
}