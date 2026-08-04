package com.tomclaw.appsend.screen.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.util.adapter.ItemBinder
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.search.di.SearchModule
import com.tomclaw.appsend.screen.settings.createSettingsActivityIntent
import com.tomclaw.appsend.util.ZipParcelable
import com.tomclaw.appsend.util.getParcelableCompat
import javax.inject.Inject

class SearchActivity : AppCompatActivity(), SearchPresenter.SearchRouter {

    @Inject
    lateinit var presenter: SearchPresenter

    @Inject
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    /** Tags to start with — a tag tapped on an app page, if any. */
    private val initialTags: List<String> by lazy {
        intent.getStringArrayListExtra(EXTRA_TAGS).orEmpty()
    }

    private lateinit var searchView: SearchView

    /**
     * Back gives up the search — the same step as the arrow in the
     * toolbar, once the keyboard is out of the way. Enabled only while
     * there is a search to give up, so that when there isn't the system
     * runs its own predictive back out of the screen.
     */
    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            // This callback and the keyboard's carry the same priority,
            // and ours is registered later — so back arrives here
            // instead of putting the keyboard away. It still has to be
            // put away, and that is a step of its own.
            if (searchView.hideKeyboard()) return
            presenter.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val presenterState = savedInstanceState
            ?.getParcelableCompat(KEY_PRESENTER_STATE, ZipParcelable::class.java)
            ?.restore<Bundle>()
        appComponent
            .searchComponent(
                SearchModule(
                    context = this,
                    initialTags = initialTags,
                    state = presenterState
                )
            )
            .inject(activity = this)

        setContentView(R.layout.activity_search)

        onBackPressedDispatcher.addCallback(backCallback)

        setupToolbar()

        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        searchView = SearchViewImpl(window.decorView, adapter)

        presenter.attachView(searchView)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            // The query field fills the toolbar, so there is no room for
            // a title — and nothing to say that the field doesn't.
            setDisplayShowTitleEnabled(false)
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
        // Arriving with tags already applied means results are on
        // screen; popping the keyboard over them would be in the way.
        if (initialTags.isEmpty()) {
            searchView.requestQueryFocus()
        }
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_PRESENTER_STATE, ZipParcelable(presenter.saveState()))
    }

    override fun setBackCallbackEnabled(enabled: Boolean) {
        backCallback.isEnabled = enabled
    }

    override fun leaveScreen() {
        finish()
    }

    override fun openSettingsScreen() {
        startActivity(createSettingsActivityIntent(this))
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

/**
 * Opens search. Pass [tags] to start with those filters applied — the
 * screen is the same either way, they are just criteria it begins with,
 * and the visitor can drop them or add text.
 */
fun createSearchActivityIntent(context: Context, tags: List<String> = emptyList()): Intent =
    Intent(context, SearchActivity::class.java)
        .apply {
            if (tags.isNotEmpty()) {
                putStringArrayListExtra(EXTRA_TAGS, ArrayList(tags))
            }
        }

private const val EXTRA_TAGS = "tags"
private const val KEY_PRESENTER_STATE = "presenter_state"
