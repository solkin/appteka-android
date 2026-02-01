package com.tomclaw.appsend.screen.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.util.adapter.ItemBinder
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.search.di.SearchModule
import com.tomclaw.appsend.util.updateTheme
import javax.inject.Inject

class SearchActivity : AppCompatActivity(), SearchPresenter.SearchRouter {

    @Inject
    lateinit var presenter: SearchPresenter

    @Inject
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    override fun onCreate(savedInstanceState: Bundle?) {
        updateTheme()
        super.onCreate(savedInstanceState)

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        appComponent
            .searchComponent(SearchModule(context = this, presenterState))
            .inject(activity = this)

        setContentView(R.layout.activity_search)

        setupToolbar()

        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val rootView = window.decorView
        val searchView = SearchViewImpl(rootView, adapter)

        presenter.attachView(searchView)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
            title = getString(R.string.search_app)
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.attachRouter(this)
    }

    override fun onStop() {
        presenter.detachRouter()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        // Request focus on query edit text after resume
        findViewById<android.widget.EditText>(R.id.query_edit)?.requestFocus()
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(KEY_PRESENTER_STATE, presenter.saveState())
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun openAppScreen(appId: String, title: String) {
        val intent = com.tomclaw.appsend.screen.details.createDetailsActivityIntent(
            context = this,
            appId = appId,
            label = title,
            moderation = false,
            finishOnly = true
        )
        startActivity(intent)
    }

}

fun createSearchActivityIntent(context: Context): Intent =
    Intent(context, SearchActivity::class.java)

private const val KEY_PRESENTER_STATE = "presenter_state"

