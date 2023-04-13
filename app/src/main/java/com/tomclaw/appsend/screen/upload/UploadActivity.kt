package com.tomclaw.appsend.screen.upload

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.main.item.CommonItem
import com.tomclaw.appsend.main.local.SelectLocalAppActivity.SELECTED_ITEM
import com.tomclaw.appsend.main.local.SelectLocalAppActivity.createSelectAppActivity
import com.tomclaw.appsend.screen.details.createDetailsActivityIntent
import com.tomclaw.appsend.screen.upload.di.UploadModule
import com.tomclaw.appsend.upload.createUploadIntent
import com.tomclaw.appsend.util.getParcelableExtraCompat
import javax.inject.Inject

class UploadActivity : AppCompatActivity(), UploadPresenter.UploadRouter {

    @Inject
    lateinit var presenter: UploadPresenter

    @Inject
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    @Inject
    lateinit var preferences: UploadPreferencesProvider

    private val selectAppResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val info: CommonItem = result.data?.getParcelableExtraCompat(
                    SELECTED_ITEM,
                    CommonItem::class.java
                ) ?: return@registerForActivityResult
                presenter.onAppSelected(info)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val info = intent.getParcelableExtra<CommonItem?>(EXTRA_PACKAGE_INFO)

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .uploadComponent(UploadModule(this, info, presenterState))
            .inject(activity = this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_activity)

        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val view = UploadViewImpl(window.decorView, preferences, adapter)

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

    override fun openSelectAppScreen() {
        val intent = createSelectAppActivity(this, null)
        selectAppResultLauncher.launch(intent)
    }

    override fun openDetailsScreen(appId: String, label: String?) {
        val intent = createDetailsActivityIntent(
            context = this,
            appId = appId,
            label = label.orEmpty(),
            finishOnly = true
        )
        startActivity(intent)
    }

    override fun startUpload(item: CommonItem) {
        val intent = createUploadIntent(context = this, item)
        startService(intent)
    }

    override fun leaveScreen() {
        finish()
    }

}

fun createUploadActivityIntent(
    context: Context,
    item: CommonItem?,
): Intent = Intent(context, UploadActivity::class.java)
    .putExtra(EXTRA_PACKAGE_INFO, item)

private const val EXTRA_PACKAGE_INFO = "package_info"
private const val KEY_PRESENTER_STATE = "presenter_state"
