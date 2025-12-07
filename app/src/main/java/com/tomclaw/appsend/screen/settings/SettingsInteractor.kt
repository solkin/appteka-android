package com.tomclaw.appsend.screen.settings

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import java.io.File

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
    private val appsDir: File,
    private val resourceProvider: SettingsResourceProvider,
    private val schedulers: SchedulersFactory
) : SettingsInteractor {

    private val preferences: SharedPreferences = context.getSharedPreferences(
        context.packageName + "_preferences", MODE_PRIVATE
    )

    override fun clearCache(): Completable = Completable.fromAction {
        val files = appsDir.listFiles { file ->
            file.name.endsWith(".apk")
        } ?: emptyArray()
        files.forEach { it.delete() }
    }
        .subscribeOn(schedulers.io())

    override fun observePreferenceChanges(): Observable<PreferenceChange> {
        return Observable.create { emitter ->
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                val value = when (key) {
                    resourceProvider.getPrefDarkThemeKey() -> prefs.getBoolean(key, false)
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

