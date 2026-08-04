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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

interface SettingsInteractor {

    fun clearCache(): Completable

    fun observePreferenceChanges(): Observable<PreferenceChange>

    /**
     * The content this account hides while browsing. Unlike everything
     * else on this screen the value lives on the server, so that it
     * follows the account instead of the install — which also means an
     * anonymous caller gets an error here, and the screen offers to
     * sign in rather than pretending the switches do anything.
     */
    fun loadContentFilter(): Observable<Set<ContentFlag>>

    fun updateContentFilter(flags: Set<ContentFlag>): Observable<Set<ContentFlag>>

}

data class PreferenceChange(
    val key: String,
    val value: Any?
)

class SettingsInteractorImpl(
    private val context: Context,
    private val api: StoreApi,
    private val apkStorage: ApkStorage,
    private val resourceProvider: SettingsResourceProvider,
    private val schedulers: SchedulersFactory
) : SettingsInteractor {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun clearCache(): Completable = Completable.fromAction {
        apkStorage.clearAll()
    }
        .subscribeOn(schedulers.io())

    override fun loadContentFilter(): Observable<Set<ContentFlag>> {
        return api
            .getProfile(userId = null)
            .map { ContentFlag.fromCodes(it.result.profile.contentFilter) }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun updateContentFilter(flags: Set<ContentFlag>): Observable<Set<ContentFlag>> {
        // Same multipart contract as the rest of the profile: a part
        // that is present replaces the column, and an empty one clears
        // it. Only this part is sent, so the name, bio and avatar are
        // left exactly as they are.
        val value = ContentFlag.codesOf(flags).joinToString(separator = ",")
        return api
            .updateProfile(null, null, null, value.toRequestBody(TEXT_PLAIN))
            .map { ContentFlag.fromCodes(it.result.profile.contentFilter) }
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
