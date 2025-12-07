package com.tomclaw.appsend.screen.settings

import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import com.github.machinarius.preferencefragment.PreferenceFragment
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.settings.di.SettingsModule
import javax.inject.Inject

class SettingsFragment : PreferenceFragment(), SettingsPresenter.SettingsRouter {

    @Inject
    lateinit var presenter: SettingsPresenter

    private lateinit var settingsView: SettingsView

    override fun onCreate(savedInstanceState: Bundle?) {
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .settingsComponent(SettingsModule(requireContext(), presenterState))
            .inject(fragment = this)

        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)

        settingsView = SettingsViewImpl(this)

        // Setup clear cache preference click
        findPreference(getString(R.string.pref_clear_cache))?.setOnPreferenceClickListener {
            (settingsView as SettingsViewImpl).onClearCacheClick()
            true
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(settingsView)
        presenter.attachRouter(this)
    }

    override fun onStop() {
        presenter.detachRouter()
        presenter.detachView()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(KEY_PRESENTER_STATE, presenter.saveState())
    }

    override fun finishActivity() {
        requireActivity().finish()
    }

    override fun restartActivity() {
        val intent = requireActivity().intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        requireActivity().finish()
        requireActivity().overridePendingTransition(0, 0)
        startActivity(intent)
    }

    override fun setResultOk() {
        requireActivity().setResult(android.app.Activity.RESULT_OK)
    }

}

private const val KEY_PRESENTER_STATE = "presenter_state"

