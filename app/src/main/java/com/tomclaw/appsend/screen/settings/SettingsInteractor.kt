package com.tomclaw.appsend.screen.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.tomclaw.appsend.download.ApkStorage
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface SettingsInteractor {

    fun clearCache(): Completable

    fun observePreferenceChanges(): Observable<PreferenceChange>

}

data class PreferenceChange(
    val key: String,
    val value: Any?
)

class SettingsInteractorImpl(
    private val context: Context,
    private val apkStorage: ApkStorage,
    private val resourceProvider: SettingsResourceProvider,
    private val schedulers: SchedulersFactory
) : SettingsInteractor {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun clearCache(): Completable = Completable.fromAction {
        apkStorage.clearAll()
    }
        .subscribeOn(schedulers.io())

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

}
