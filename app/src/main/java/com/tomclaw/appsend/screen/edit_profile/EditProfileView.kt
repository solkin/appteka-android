package com.tomclaw.appsend.screen.edit_profile

import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.util.showWithAlphaAnimation
import com.tomclaw.appsend.view.UserIconViewImpl
import io.reactivex.rxjava3.core.Observable

interface EditProfileView {

    fun setName(value: String)

    fun setBio(value: String)

    fun bindUserIcon(icon: UserIcon)

    fun setBioCounter(currentLength: Int, maxLength: Int)

    /**
     * Toggle the avatar editor. Hidden when the viewer lacks the
     * AvatarUpload capability — per product decision the section
     * disappears entirely instead of showing a denial banner.
     */
    fun showAvatarEditor()

    fun hideAvatarEditor()

    fun showRemoveAvatarButton()

    fun hideRemoveAvatarButton()

    /** Show a freshly-picked local image in the avatar preview. */
    fun showAvatarFromUri(uri: Uri)

    /** Reset the preview to whatever the server-side icon currently is. */
    fun resetAvatarPreview()

    fun showBioEditor()

    fun hideBioEditor()

    fun enableSaveButton()

    fun disableSaveButton()

    fun showProgress()

    fun showContent()

    fun showError(text: String)

    fun showSuccess(text: String)

    fun navigationClicks(): Observable<Unit>

    fun nameChanged(): Observable<String>

    fun bioChanged(): Observable<String>

    fun changeAvatarClicks(): Observable<Unit>

    fun removeAvatarClicks(): Observable<Unit>

    fun saveClicks(): Observable<Unit>

}

class EditProfileViewImpl(view: View) : EditProfileView {

    private val rootView: View = view.findViewById(R.id.root_view)
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val avatarSection: View = view.findViewById(R.id.avatar_section)
    private val avatarChangeButton: MaterialButton = view.findViewById(R.id.avatar_change_button)
    private val avatarRemoveButton: MaterialButton = view.findViewById(R.id.avatar_remove_button)
    private val nameInputLayout: TextInputLayout = view.findViewById(R.id.name_input_layout)
    private val nameInput: TextInputEditText = view.findViewById(R.id.name_input)
    private val bioSection: View = view.findViewById(R.id.bio_section)
    private val bioInputLayout: TextInputLayout = view.findViewById(R.id.bio_input_layout)
    private val bioInput: TextInputEditText = view.findViewById(R.id.bio_input)
    private val bioCounter: TextView = view.findViewById(R.id.bio_counter)
    private val saveButton: MaterialButton = view.findViewById(R.id.save_button)
    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)

    private val userIconView = UserIconViewImpl(view.findViewById(R.id.avatar_holder))

    private val navigationRelay = PublishRelay.create<Unit>()
    private val nameChangedRelay = PublishRelay.create<String>()
    private val bioChangedRelay = PublishRelay.create<String>()
    private val changeAvatarRelay = PublishRelay.create<Unit>()
    private val removeAvatarRelay = PublishRelay.create<Unit>()
    private val saveRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setTitle(R.string.edit_profile_title)
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }

        nameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                nameChangedRelay.accept(s.toString())
            }
        })

        bioInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                bioChangedRelay.accept(s.toString())
            }
        })

        avatarChangeButton.setOnClickListener { changeAvatarRelay.accept(Unit) }
        avatarRemoveButton.setOnClickListener { removeAvatarRelay.accept(Unit) }
        saveButton.setOnClickListener { saveRelay.accept(Unit) }
    }

    override fun setName(value: String) {
        if (nameInput.text?.toString() != value) {
            nameInput.setText(value)
            nameInput.setSelection(nameInput.text?.length ?: 0)
        }
    }

    override fun setBio(value: String) {
        if (bioInput.text?.toString() != value) {
            bioInput.setText(value)
            bioInput.setSelection(bioInput.text?.length ?: 0)
        }
    }

    override fun bindUserIcon(icon: UserIcon) {
        userIconView.bind(icon)
    }

    override fun setBioCounter(currentLength: Int, maxLength: Int) {
        bioCounter.text = "$currentLength/$maxLength"
    }

    override fun showAvatarEditor() {
        avatarSection.show()
    }

    override fun hideAvatarEditor() {
        avatarSection.hide()
    }

    override fun showRemoveAvatarButton() {
        avatarRemoveButton.show()
    }

    override fun hideRemoveAvatarButton() {
        avatarRemoveButton.hide()
    }

    override fun showAvatarFromUri(uri: Uri) {
        userIconView.bindLocalImage(uri)
    }

    override fun resetAvatarPreview() {
        userIconView.clearLocalImage()
    }

    override fun showBioEditor() {
        bioSection.show()
    }

    override fun hideBioEditor() {
        bioSection.hide()
    }

    override fun enableSaveButton() {
        saveButton.isEnabled = true
    }

    override fun disableSaveButton() {
        saveButton.isEnabled = false
    }

    override fun showProgress() {
        overlayProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun showError(text: String) {
        Snackbar.make(rootView, text, Snackbar.LENGTH_LONG).show()
    }

    override fun showSuccess(text: String) {
        Snackbar.make(rootView, text, Snackbar.LENGTH_SHORT).show()
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay
    override fun nameChanged(): Observable<String> = nameChangedRelay
    override fun bioChanged(): Observable<String> = bioChangedRelay
    override fun changeAvatarClicks(): Observable<Unit> = changeAvatarRelay
    override fun removeAvatarClicks(): Observable<Unit> = removeAvatarRelay
    override fun saveClicks(): Observable<Unit> = saveRelay

}
