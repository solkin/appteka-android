package com.tomclaw.appsend.screen.edit_profile

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.profile.api.ProfileResponse
import com.tomclaw.appsend.util.ImageCompressor
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

interface EditProfileInteractor {

    fun loadProfile(): Observable<ProfileResponse>

    fun updateProfile(request: EditProfileRequest): Observable<ProfileResponse>

}

class EditProfileInteractorImpl(
    private val api: StoreApi,
    private val compressor: ImageCompressor,
    private val schedulers: SchedulersFactory,
) : EditProfileInteractor {

    override fun loadProfile(): Observable<ProfileResponse> {
        return api
            .getProfile(userId = null)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun updateProfile(request: EditProfileRequest): Observable<ProfileResponse> {
        // Each field is encoded so that absent ⇒ Retrofit omits the
        // part, present-empty ⇒ we ship a 0-byte text or file part.
        // The server reads MultipartForm.Value/File directly and uses
        // key existence as the "field touched" signal.
        val name = if (request.nameSet) (request.name.orEmpty()).toTextPart() else null
        val bio = if (request.bioSet) (request.bio.orEmpty()).toTextPart() else null
        val avatar: MultipartBody.Part? = when {
            !request.avatarSet -> null
            request.avatarUri != null -> MultipartBody.Part.createFormData(
                "avatar",
                "avatar.jpg",
                compressor.asRequestBody(request.avatarUri),
            )
            // Clear: 0-byte file part with the same field name. The
            // server's empty-file branch nulls both avatar columns.
            else -> MultipartBody.Part.createFormData(
                "avatar",
                "",
                EMPTY_BYTES.toRequestBody(IMAGE_JPEG),
            )
        }

        return api
            .updateProfile(name, bio, avatar)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    private fun String.toTextPart(): RequestBody = toRequestBody(TEXT_PLAIN)

    private companion object {
        val TEXT_PLAIN = "text/plain".toMediaType()
        val IMAGE_JPEG = "image/jpeg".toMediaType()
        val EMPTY_BYTES = ByteArray(0)
    }
}
