package com.tomclaw.appsend.screen.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.about.di.AboutModule
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.ThemeHelper
import javax.inject.Inject

class AboutActivity : AppCompatActivity(), AboutPresenter.AboutRouter {

    @Inject
    lateinit var presenter: AboutPresenter

    @Inject
    lateinit var analytics: Analytics

    override fun onCreate(savedInstanceState: Bundle?) {
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .aboutComponent(AboutModule(context = this, presenterState))
            .inject(activity = this)
        ThemeHelper.updateTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_activity)

        val view = AboutViewImpl(window.decorView)

        presenter.attachView(view)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-about-screen")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        presenter.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        presenter.attachRouter(this)
    }

    override fun onStop() {
        presenter.detachRouter()
        super.onStop()
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(KEY_PRESENTER_STATE, presenter.saveState())
    }

    override fun openFeedbackEmail(addr: String, subject: String, text: String) {
        val uri = Uri.fromParts("mailto", addr, null)
        val intent = Intent(Intent.ACTION_SENDTO, uri)
            .putExtra(Intent.EXTRA_SUBJECT, subject)
            .putExtra(Intent.EXTRA_TEXT, text)
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.send_email)))
        } catch (ex: Throwable) {
            Toast.makeText(this, getString(R.string.no_email_clients), Toast.LENGTH_SHORT).show()
        }
        analytics.trackEvent("click-email-feedback")
    }

    override fun openForumDiscussLink() {
        openUrl(url = getString(R.string.forum_url))
        analytics.trackEvent("click-4pda-forum")
    }

    override fun openTelegramGroupLink() {
        openUrl(url = getString(R.string.telegram_group_url))
        analytics.trackEvent("click-telegram-group")
    }

    override fun openLegalInfoLink() {
        openUrl(url = getString(R.string.legal_info_url))
        analytics.trackEvent("click-legal-info")
    }

    override fun leaveScreen() {
        finish()
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (ignored: Throwable) {
        }
    }

}

fun createAboutActivityIntent(
    context: Context,
): Intent = Intent(context, AboutActivity::class.java)

private const val KEY_PRESENTER_STATE = "presenter_state"
