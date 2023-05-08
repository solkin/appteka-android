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
import com.tomclaw.appsend.dto.LocalAppEntity
import com.tomclaw.appsend.main.item.CommonItem
import com.tomclaw.appsend.main.local.SelectLocalAppActivity.SELECTED_ITEM
import com.tomclaw.appsend.main.local.SelectLocalAppActivity.createSelectAppActivity
import com.tomclaw.appsend.screen.details.createDetailsActivityIntent
import com.tomclaw.appsend.screen.upload.di.UploadModule
import com.tomclaw.appsend.upload.UploadInfo
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
                val entity = LocalAppEntity(
                    label = info.label,
                    packageName = info.packageName,
                    version = info.version,
                    path = info.path,
                    size = info.size,
                    packageInfo = info.packageInfo
                )
                presenter.onAppSelected(entity)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val entity = intent.getParcelableExtra<LocalAppEntity?>(EXTRA_APP_ENTITY)
        val info = intent.getParcelableExtra<UploadInfo?>(EXTRA_UPLOAD_INFO)

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .uploadComponent(UploadModule(this, entity, info, presenterState))
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

    override fun startUpload(entity: LocalAppEntity, info: UploadInfo) {
        val intent = createUploadIntent(context = this, entity, info)
        startService(intent)
    }

    override fun leaveScreen() {
        finish()
    }

}

fun createUploadActivityIntent(
    context: Context,
    entity: LocalAppEntity?,
    info: UploadInfo?,
): Intent = Intent(context, UploadActivity::class.java)
    .putExtra(EXTRA_APP_ENTITY, entity)
    .putExtra(EXTRA_UPLOAD_INFO, info)

private const val EXTRA_APP_ENTITY = "app_entity"
private const val EXTRA_UPLOAD_INFO = "upload_info"
private const val KEY_PRESENTER_STATE = "presenter_state"
