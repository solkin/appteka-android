package com.tomclaw.appsend.screen.restrict

import android.content.res.Resources
import com.tomclaw.appsend.R

/**
 * The words for one restrict flow. Blocking and unpublishing share a
 * screen but must never share wording: one is permanent and the author
 * cannot appeal it, the other is an invitation to fix the app.
 */
interface RestrictResourceProvider {

    fun title(label: String): String

    fun description(): String

    fun submitLabel(): String

    /** Shown under the button, or null when there is nothing to warn about. */
    fun warning(): String?

}

class UnlinkResourceProvider(
    private val resources: Resources,
) : RestrictResourceProvider {

    override fun title(label: String): String =
        resources.getString(R.string.unlink_of, label)

    override fun description(): String =
        resources.getString(R.string.restrict_block_description)

    override fun submitLabel(): String = resources.getString(R.string.unlink_file)

    override fun warning(): String = resources.getString(R.string.restrict_block_warning)

}

class UnpublishResourceProvider(
    private val resources: Resources,
) : RestrictResourceProvider {

    override fun title(label: String): String =
        resources.getString(R.string.unpublish_of, label)

    override fun description(): String =
        resources.getString(R.string.restrict_unpublish_description)

    override fun submitLabel(): String = resources.getString(R.string.unpublish_file)

    override fun warning(): String? = null

}
