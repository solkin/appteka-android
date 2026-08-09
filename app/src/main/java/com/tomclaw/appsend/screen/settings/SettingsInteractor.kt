package com.tomclaw.appsend.screen.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.core.content.ContentFlag
import com.tomclaw.appsend.download.ApkStorage
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale

interface SettingsInteractor {

    fun clearCache(): Completable

    fun observePreferenceChanges(): Observable<PreferenceChange>

    /**
     * The categories on offer and the ones this account hides. Both come
     * from the server: the vocabulary because the app keeps no copy of
     * it, the selection because it follows the account instead of the
     * install — which also means an anonymous caller gets an error here,
     * and the screen offers to sign in rather than pretending the
     * switches do anything.
     */
    fun loadContentFilter(): Observable<ContentFilterState>

    /** Saves the hidden set and answers with what the server kept. */
    fun updateContentFilter(codes: Set<String>): Observable<Set<String>>

}

/**
 * Everything the content block needs in one answer: what can be
 * offered, and what is currently hidden. Kept together because a
 * selection without its vocabulary cannot be drawn.
 */
data class ContentFilterState(
    val catalog: List<ContentFlag>,
    val hidden: Set<String>,
)

data class PreferenceChange(
    val key: String,
    val value: Any?
)

class SettingsInteractorImpl(
    private val context: Context,
    private val api: StoreApi,
    private val locale: Locale,
    private val apkStorage: ApkStorage,
    private val resourceProvider: SettingsResourceProvider,
    private val schedulers: SchedulersFactory
) : SettingsInteractor {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun clearCache(): Completable = Completable.fromAction {
        apkStorage.clearAll()
    }
        .subscribeOn(schedulers.io())

    override fun loadContentFilter(): Observable<ContentFilterState> {
        return Single
            .zip(
                api.getContentFlags(locale.language).map { it.result.flags },
                api.getProfile(userId = null).map { it.result.profile.contentFilter.orEmpty() },
            ) { catalog, hidden -> ContentFilterState(catalog, hidden.toSet()) }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun updateContentFilter(codes: Set<String>): Observable<Set<String>> {
        // Same multipart contract as the rest of the profile: a part
        // that is present replaces the column, and an empty one clears
        // it. Only this part is sent, so the name, bio and avatar are
        // left exactly as they are.
        val value = codes.joinToString(separator = ",")
        return api
            .updateProfile(null, null, null, value.toRequestBody(TEXT_PLAIN))
            .map { it.result.profile.contentFilter.orEmpty().toSet() }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun observePreferenceChanges(): Observable<PreferenceChange> {
        return Observable.create { emitter ->
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                val value = when (key) {
                    resourceProvider.getPrefDarkThemeKey() -> prefs.getBoolean(key, false)
                    resourceProvider.getPrefThemeModeKey() -> prefs.getInt(key, -1)
                    resourceProvider.getPrefDynamicColorsKey() -> prefs.getBoolean(key, true)
                    resourceProvider.getPrefShowSystemKey() -> prefs.getBoolean(key, false)
                    resourceProvider.getPrefSortOrderKey() -> prefs.getString(key, "")
                    else -> null
                }
                key?.let {
                    emitter.onNext(PreferenceChange(key, value))
                }
            }
            preferences.registerOnSharedPreferenceChangeListener(listener)
            emitter.setCancellable {
                preferences.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }
            .subscribeOn(schedulers.io())
    }

    private companion object {
        val TEXT_PLAIN = "text/plain".toMediaType()
    }

}
