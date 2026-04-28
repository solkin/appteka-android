package com.tomclaw.appsend.screen.edit_profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.R
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.screen.avatar_crop.createAvatarCropActivityIntent
import com.tomclaw.appsend.screen.avatar_crop.extractAvatarCropResultUri
import com.tomclaw.appsend.screen.edit_profile.di.EditProfileModule
import com.tomclaw.appsend.util.Analytics
import javax.inject.Inject

class EditProfileActivity : AppCompatActivity(), EditProfilePresenter.EditProfileRouter {

    @Inject
    lateinit var presenter: EditProfilePresenter

    @Inject
    lateinit var analytics: Analytics

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) presenter.onAvatarPicked(uri)
        }

    private val cropLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val cropped = extractAvatarCropResultUri(result.data)
                if (cropped != null) {
                    presenter.onAvatarCropped(cropped)
                } else {
                    presenter.onAvatarPickFailed()
                }
            }
            // RESULT_CANCELED ⇒ user backed out of the cropper,
            // nothing to do; the editor stays as it was.
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        appComponent
            .editProfileComponent(EditProfileModule(this, presenterState))
            .inject(activity = this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile_activity)

        val view = EditProfileViewImpl(window.decorView)
        presenter.attachView(view)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-edit-profile-screen")
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.attachRouter(this)
    }

    override fun onStop() {
        presenter.detachRouter()
        super.onStop()
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(KEY_PRESENTER_STATE, presenter.saveState())
    }

    override fun openImagePicker() {
        val request = PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
            .build()
        pickImageLauncher.launch(request)
    }

    override fun openCropper(srcUri: Uri) {
        cropLauncher.launch(createAvatarCropActivityIntent(this, srcUri))
    }

    override fun leaveScreen(success: Boolean) {
        if (success) {
            setResult(RESULT_OK)
        } else {
            setResult(RESULT_CANCELED)
        }
        finish()
    }

}

fun createEditProfileActivityIntent(context: Context): Intent =
    Intent(context, EditProfileActivity::class.java)

private const val KEY_PRESENTER_STATE = "presenter_state"
