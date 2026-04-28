package com.tomclaw.appsend.screen.edit_profile

import android.net.Uri
import android.os.Bundle
import com.tomclaw.appsend.core.permissions.CapabilityAction
import com.tomclaw.appsend.core.permissions.CapabilityPolicy
import com.tomclaw.appsend.core.permissions.UserCapabilitiesProvider
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.profile.api.Profile
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import java.io.File

interface EditProfilePresenter {

    fun attachView(view: EditProfileView)

    fun detachView()

    fun attachRouter(router: EditProfileRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onAvatarPicked(uri: Uri)

    fun onAvatarCropped(uri: Uri)

    fun onAvatarPickFailed()

    fun onBackPressed()

    interface EditProfileRouter {

        fun openImagePicker()

        /** Hand a freshly picked image off to the cropper. */
        fun openCropper(srcUri: Uri)

        fun leaveScreen(success: Boolean)

    }

}

class EditProfilePresenterImpl(
    private val interactor: EditProfileInteractor,
    private val resourceProvider: EditProfileResourceProvider,
    private val capabilitiesProvider: UserCapabilitiesProvider,
    private val schedulers: SchedulersFactory,
    state: Bundle?,
) : EditProfilePresenter {

    private var view: EditProfileView? = null
    private var router: EditProfilePresenter.EditProfileRouter? = null

    private var name: String = state?.getString(KEY_NAME).orEmpty()
    private var bio: String = state?.getString(KEY_BIO).orEmpty()
    private var initialName: String = state?.getString(KEY_INITIAL_NAME).orEmpty()
    private var initialBio: String = state?.getString(KEY_INITIAL_BIO).orEmpty()
    // Restored across recreation so a back-from-cropper trip does not
    // leave the avatar slot blank — the screen is rebuilt from
    // savedInstanceState rather than re-fetching the profile.
    private var initialUserIcon: UserIcon? = state?.getParcelable(KEY_INITIAL_USER_ICON)
    private var nameRegex: String? = state?.getString(KEY_NAME_REGEX)
    // The cropped pick lives in the picked-media disk LRU cache; if it
    // was evicted (or wiped) between save-state and restore, treat the
    // pick as gone instead of leaving a dangling URI that would render
    // a blank avatar yet keep Save enabled.
    private var pendingAvatarUri: Uri? = state?.getParcelable<Uri>(KEY_PENDING_AVATAR)
        ?.takeIf { it.referencedFileExists() }
    private var removeAvatar: Boolean = state?.getBoolean(KEY_REMOVE_AVATAR, false) ?: false
    private var profileLoaded: Boolean = state?.getBoolean(KEY_PROFILE_LOADED, false) ?: false

    private val subscriptions = CompositeDisposable()

    private val hasInitialAvatar: Boolean
        get() = initialUserIcon?.image != null

    private val avatarUploadAllowed: Boolean
        get() = CapabilityPolicy.isAllowed(
            action = CapabilityAction.AVATAR_UPLOAD,
            capabilities = capabilitiesProvider.getCapabilities(),
            allowOnUnknown = false,
        )

    private val bioEditAllowed: Boolean
        get() = CapabilityPolicy.isAllowed(
            action = CapabilityAction.BIO_EDIT,
            capabilities = capabilitiesProvider.getCapabilities(),
            allowOnUnknown = false,
        )

    override fun attachView(view: EditProfileView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.nameChanged().subscribe { value ->
            if (value != name) {
                name = value
                render()
            }
        }
        subscriptions += view.bioChanged().subscribe { value ->
            if (value != bio) {
                bio = value
                render()
            }
        }
        subscriptions += view.changeAvatarClicks().subscribe { router?.openImagePicker() }
        subscriptions += view.removeAvatarClicks().subscribe { onRemoveAvatar() }
        subscriptions += view.saveClicks().subscribe { onSave() }

        if (profileLoaded) {
            render()
        } else {
            loadProfile()
        }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: EditProfilePresenter.EditProfileRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState(): Bundle = Bundle().apply {
        putString(KEY_NAME, name)
        putString(KEY_BIO, bio)
        putString(KEY_INITIAL_NAME, initialName)
        putString(KEY_INITIAL_BIO, initialBio)
        putString(KEY_NAME_REGEX, nameRegex)
        putParcelable(KEY_PENDING_AVATAR, pendingAvatarUri)
        putBoolean(KEY_REMOVE_AVATAR, removeAvatar)
        putBoolean(KEY_PROFILE_LOADED, profileLoaded)
        putParcelable(KEY_INITIAL_USER_ICON, initialUserIcon)
    }

    override fun onAvatarPicked(uri: Uri) {
        // The picker hands back the source URI; the cropper turns
        // it into the squared-up version we want to upload.
        router?.openCropper(uri)
    }

    override fun onAvatarCropped(uri: Uri) {
        pendingAvatarUri = uri
        removeAvatar = false
        render()
    }

    override fun onAvatarPickFailed() {
        view?.showError(resourceProvider.getServiceError())
    }

    private fun onRemoveAvatar() {
        pendingAvatarUri = null
        removeAvatar = true
        render()
    }

    override fun onBackPressed() {
        router?.leaveScreen(success = false)
    }

    private fun loadProfile() {
        view?.showProgress()
        subscriptions += interactor.loadProfile()
            .observeOn(schedulers.mainThread())
            .subscribe(
                { response ->
                    onProfileLoaded(response.profile)
                    view?.showContent()
                },
                {
                    // Empty form is a dead-end (Save is disabled, no
                    // initial values to edit); bail out instead of
                    // leaving the user staring at it.
                    view?.showContent()
                    view?.showError(resourceProvider.getLoadFailedError())
                    router?.leaveScreen(success = false)
                },
            )
    }

    private fun onProfileLoaded(profile: Profile) {
        initialName = profile.name.orEmpty()
        initialBio = profile.bio.orEmpty()
        nameRegex = profile.nameRegex
        initialUserIcon = profile.userIcon
        if (!profileLoaded) {
            // Only seed the editable fields on the first load —
            // honouring any in-flight edit if the screen is rebuilt
            // after a config change.
            name = initialName
            bio = initialBio
        }
        profileLoaded = true
        render()
    }

    /**
     * Single source of truth for view state. Every state change (load,
     * pick, remove, save, text input) ends with a [render] call so the
     * view is always a pure projection of the presenter — no scattered
     * `bindXxx()` calls to forget.
     */
    private fun render() {
        val view = view ?: return

        // Capability-gated section visibility. A capability snapshot
        // doesn't change while the editor is open, but rendering it on
        // every pass is free and keeps the projection complete.
        if (avatarUploadAllowed) view.showAvatarEditor() else view.hideAvatarEditor()
        if (bioEditAllowed) view.showBioEditor() else view.hideBioEditor()

        view.setName(name)
        view.setBio(bio)
        view.setBioCounter(bio.length, BIO_MAX_LENGTH)

        initialUserIcon?.let(view::bindUserIcon)
        when {
            pendingAvatarUri != null -> pendingAvatarUri?.let(view::showAvatarFromUri)
            removeAvatar -> view.resetAvatarPreview()
        }

        val canRemoveAvatar = avatarUploadAllowed &&
            ((hasInitialAvatar && !removeAvatar) || pendingAvatarUri != null)
        if (canRemoveAvatar) view.showRemoveAvatarButton() else view.hideRemoveAvatarButton()

        if (validate() == null && hasChanges()) view.enableSaveButton() else view.disableSaveButton()
    }

    private fun validate(): ValidationError? {
        val regex = nameRegex
        if (name.isNotEmpty() && regex != null && !name.matches(regex.toRegex())) {
            return ValidationError.InvalidName
        }
        if (bio.length > BIO_MAX_LENGTH) {
            return ValidationError.BioTooLong
        }
        return null
    }

    private fun hasChanges(): Boolean {
        if (name != initialName) return true
        if (bioEditAllowed && bio != initialBio) return true
        if (avatarUploadAllowed && (pendingAvatarUri != null || removeAvatar)) return true
        return false
    }

    private fun buildRequest(): EditProfileRequest {
        val nameSet = name != initialName
        val bioSet = bioEditAllowed && bio != initialBio
        val avatarSet = avatarUploadAllowed && (pendingAvatarUri != null || removeAvatar)
        return EditProfileRequest(
            nameSet = nameSet,
            name = if (nameSet) name.takeIf { it.isNotEmpty() } else null,
            bioSet = bioSet,
            bio = if (bioSet) bio.takeIf { it.isNotEmpty() } else null,
            avatarSet = avatarSet,
            avatarUri = if (avatarSet) pendingAvatarUri else null,
        )
    }

    private fun onSave() {
        val view = view ?: return
        validate()?.let { error ->
            view.showError(
                when (error) {
                    ValidationError.InvalidName -> resourceProvider.getInvalidNameError()
                    ValidationError.BioTooLong -> resourceProvider.getBioTooLongError()
                }
            )
            return
        }
        val request = buildRequest()
        if (request.isEmpty()) {
            // Nothing changed — leave the screen as if save succeeded
            // without making a server round-trip.
            router?.leaveScreen(success = true)
            return
        }

        view.showProgress()
        view.disableSaveButton()
        subscriptions += interactor.updateProfile(request)
            .observeOn(schedulers.mainThread())
            .subscribe(
                {
                    view.showContent()
                    view.showSuccess(resourceProvider.getProfileSavedSuccess())
                    router?.leaveScreen(success = true)
                },
                {
                    view.showContent()
                    view.showError(resourceProvider.getServiceError())
                    render()
                },
            )
    }

}

private enum class ValidationError {
    InvalidName,
    BioTooLong,
}

/**
 * Best-effort existence check for a `file://` URI restored from saved
 * state — guards against a `DiskLruCache` eviction (or external wipe)
 * having dropped the cropped avatar between the activity going away
 * and coming back.
 */
private fun Uri.referencedFileExists(): Boolean {
    if ("file" != scheme) return true // not a file URI ⇒ trust it
    val path = path ?: return false
    return File(path).exists()
}

private const val KEY_NAME = "name"
private const val KEY_BIO = "bio"
private const val KEY_INITIAL_NAME = "initial_name"
private const val KEY_INITIAL_BIO = "initial_bio"
private const val KEY_NAME_REGEX = "name_regex"
private const val KEY_PENDING_AVATAR = "pending_avatar"
private const val KEY_REMOVE_AVATAR = "remove_avatar"
private const val KEY_PROFILE_LOADED = "profile_loaded"
private const val KEY_INITIAL_USER_ICON = "initial_user_icon"

private const val BIO_MAX_LENGTH = 512
