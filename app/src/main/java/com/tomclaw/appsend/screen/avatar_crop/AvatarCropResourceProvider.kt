package com.tomclaw.appsend.screen.avatar_crop

import android.content.res.Resources
import com.tomclaw.appsend.R

interface AvatarCropResourceProvider {

    fun getLoadFailedError(): String

    fun getSaveFailedError(): String

}

class AvatarCropResourceProviderImpl(
    private val resources: Resources,
) : AvatarCropResourceProvider {

    override fun getLoadFailedError(): String =
        resources.getString(R.string.avatar_crop_load_failed)

    override fun getSaveFailedError(): String =
        resources.getString(R.string.avatar_crop_save_failed)

}
