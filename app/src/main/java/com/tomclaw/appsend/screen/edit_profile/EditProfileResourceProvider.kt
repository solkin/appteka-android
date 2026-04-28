package com.tomclaw.appsend.screen.edit_profile

import android.content.res.Resources
import com.tomclaw.appsend.R

interface EditProfileResourceProvider {

    fun getInvalidNameError(): String

    fun getBioTooLongError(): String

    fun getLoadFailedError(): String

    fun getServiceError(): String

    fun getProfileSavedSuccess(): String

}

class EditProfileResourceProviderImpl(
    private val resources: Resources,
) : EditProfileResourceProvider {

    override fun getInvalidNameError(): String =
        resources.getString(R.string.edit_profile_name_invalid)

    override fun getBioTooLongError(): String =
        resources.getString(R.string.edit_profile_bio_too_long)

    override fun getLoadFailedError(): String =
        resources.getString(R.string.edit_profile_load_failed)

    override fun getServiceError(): String =
        resources.getString(R.string.edit_profile_save_failed)

    override fun getProfileSavedSuccess(): String =
        resources.getString(R.string.edit_profile_saved)

}
