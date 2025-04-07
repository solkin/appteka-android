package com.tomclaw.appsend.screen.downloads

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.details.createDetailsActivityIntent
import com.tomclaw.appsend.screen.downloads.di.DownloadsModule
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.updateTheme
import javax.inject.Inject

class DownloadsActivity : AppCompatActivity(), DownloadsPresenter.DownloadsRouter {

    @Inject
    lateinit var presenter: DownloadsPresenter

    @Inject
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    @Inject
    lateinit var analytics: Analytics

    private val invalidateDetailsResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                presenter.invalidateApps()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val userId = intent.getIntExtra(EXTRA_USER_ID, 0)
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .downloadComponent(DownloadsModule(this, userId, presenterState))
            .inject(activity = this)
        updateTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.downloads_activity)

        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val view = DownloadsViewImpl(window.decorView, adapter)

        presenter.attachView(view)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-downloads-screen")
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                presenter.onBackPressed()
            }
        })
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

    override fun openAppScreen(appId: String, title: String) {
        val intent = createDetailsActivityIntent(
            context = this,
            appId = appId,
            label = title,
            moderation = false,
            finishOnly = true
        )
        invalidateDetailsResultLauncher.launch(intent)
    }

    override fun leaveScreen() {
        finish()
    }

}

fun createDownloadsActivityIntent(
    context: Context,
    userId: Int
): Intent = Intent(context, DownloadsActivity::class.java)
    .putExtra(EXTRA_USER_ID, userId)

private const val EXTRA_USER_ID = "user_id"
private const val KEY_PRESENTER_STATE = "presenter_state"
