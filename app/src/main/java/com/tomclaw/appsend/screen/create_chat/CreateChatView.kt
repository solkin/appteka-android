package com.tomclaw.appsend.screen.create_chat

import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.imageloader.util.fetch
import com.tomclaw.appsend.util.clicks
import com.tomclaw.appsend.util.disable
import com.tomclaw.appsend.util.enable
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable

interface CreateChatView {

    fun setTitle(title: String)

    fun setDescription(description: String)

    fun setAvatar(uri: Uri)

    fun enableSubmitButton()

    fun disableSubmitButton()

    fun showProgress()

    fun showContent()

    fun showError(message: String)

    fun showValidationError(message: String)

    fun openAvatarPicker()

    fun navigationClicks(): Observable<Unit>

    fun titleChanged(): Observable<String>

    fun descriptionChanged(): Observable<String>

    fun avatarClicks(): Observable<Unit>

    fun submitClicks(): Observable<Unit>

}

class CreateChatViewImpl(
    view: View,
    private val avatarPickerLauncher: () -> Unit,
) : CreateChatView {

    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)
    private val rootView: View = view.findViewById(R.id.root_view)
    private val avatarContainer: MaterialCardView = view.findViewById(R.id.avatar_container)
    private val avatar: ImageView = view.findViewById(R.id.avatar)
    private val avatarPlaceholder: View = view.findViewById(R.id.avatar_placeholder)
    private val titleInput: TextInputEditText = view.findViewById(R.id.title_input)
    private val descriptionInput: TextInputEditText = view.findViewById(R.id.description_input)
    private val submitButton: MaterialButton = view.findViewById(R.id.submit_button)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val titleRelay = PublishRelay.create<String>()
    private val descriptionRelay = PublishRelay.create<String>()
    private val avatarRelay = PublishRelay.create<Unit>()
    private val submitRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setTitle(R.string.create_chat_title)
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
        avatarContainer.clicks(avatarRelay)
        titleInput.addTextChangedListener { text ->
            titleRelay.accept(text?.toString().orEmpty())
        }
        descriptionInput.addTextChangedListener { text ->
            descriptionRelay.accept(text?.toString().orEmpty())
        }
        submitButton.clicks(submitRelay)
    }

    override fun setTitle(title: String) {
        if (titleInput.text?.toString() != title) {
            titleInput.setText(title)
        }
    }

    override fun setDescription(description: String) {
        if (descriptionInput.text?.toString() != description) {
            descriptionInput.setText(description)
        }
    }

    override fun setAvatar(uri: Uri) {
        avatar.fetch(uri.toString()) {
            centerCrop()
            onLoading { imageView -> imageView.setImageDrawable(null) }
        }
        avatar.visibility = View.VISIBLE
        avatarPlaceholder.visibility = View.GONE
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

    override fun showError(message: String) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show()
    }

    override fun showValidationError(message: String) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun openAvatarPicker() {
        avatarPickerLauncher()
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun titleChanged(): Observable<String> = titleRelay

    override fun descriptionChanged(): Observable<String> = descriptionRelay

    override fun avatarClicks(): Observable<Unit> = avatarRelay

    override fun submitClicks(): Observable<Unit> = submitRelay

}
