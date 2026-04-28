package com.tomclaw.appsend.screen.edit_profile

import android.net.Uri

/**
 * Typed in-memory representation of a PATCH /2/user/profile body.
 *
 * Each `*Set` flag opts the corresponding column into the update —
 * absent means "leave the column alone", present means "write the
 * accompanying value (which may itself be null/empty/0-byte to
 * signal a clear)". This matches the server-side contract one to
 * one and is what the wire-side multipart construction encodes.
 */
data class EditProfileRequest(
    val nameSet: Boolean = false,
    val name: String? = null,

    val bioSet: Boolean = false,
    val bio: String? = null,

    val avatarSet: Boolean = false,
    /** non-null URI ⇒ upload that bitmap; null ⇒ clear the avatar. */
    val avatarUri: Uri? = null,
) {
    fun isEmpty(): Boolean = !nameSet && !bioSet && !avatarSet
}
