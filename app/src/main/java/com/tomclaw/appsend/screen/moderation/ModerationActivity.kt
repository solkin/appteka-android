package com.tomclaw.appsend.screen.moderation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.moderation.di.ModerationModule
import javax.inject.Inject

class ModerationActivity : AppCompatActivity(), ModerationPresenter.ModerationRouter {

    @Inject
    lateinit var presenter: ModerationPresenter

    @Inject
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    override fun onCreate(savedInstanceState: Bundle?) {
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .moderationComponent(ModerationModule(this, presenterState))
            .inject(activity = this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.moderation_activity)

        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val view = ModerationViewImpl(window.decorView, adapter)

        presenter.attachView(view)
    }

    override fun onBackPressed() {
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

    override fun openAppModerationScreen(appId: Int) {
//        val intent = createAppActivityIntent(context = this, appId = appId)
//        startActivity(intent)
    }

    override fun leaveScreen() {
        finish()
    }

}

fun createModerationActivityIntent(
    context: Context,
): Intent = Intent(context, ModerationActivity::class.java)

private const val KEY_PRESENTER_STATE = "presenter_state"
