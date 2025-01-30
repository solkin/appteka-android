package com.tomclaw.appsend.screen.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.feed.di.FeedModule
import com.tomclaw.appsend.screen.gallery.GalleryItem
import com.tomclaw.appsend.screen.gallery.createGalleryActivityIntent
import com.tomclaw.appsend.screen.profile.createProfileActivityIntent
import com.tomclaw.appsend.util.Analytics
import javax.inject.Inject

class FeedFragment : Fragment(), FeedPresenter.FeedRouter {

    @Inject
    lateinit var presenter: FeedPresenter

    @Inject
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    @Inject
    lateinit var analytics: Analytics

    override fun onCreate(savedInstanceState: Bundle?) {
        val userId = arguments?.getInt(ARG_USER_ID)
        val postId = arguments?.getInt(ARG_POST_ID)
        val withToolbar = arguments?.getBoolean(ARG_WITH_TOOLBAR, false)

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .feedComponent(FeedModule(requireContext(), userId, postId, withToolbar, presenterState))
            .inject(fragment = this)

        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-feed-fragment")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.feed_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val feedView = FeedViewImpl(view, adapter)

        presenter.attachView(feedView)
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

    override fun openProfileScreen(userId: Int) {
        val context = context ?: return
        val intent = createProfileActivityIntent(context, userId)
        startActivity(intent)
    }

    override fun openGallery(items: List<GalleryItem>, current: Int) {
        val context = context ?: return
        val intent = createGalleryActivityIntent(context, items, current)
        startActivity(intent)
    }

    override fun leaveScreen() {
        activity?.onBackPressed()
    }

}

fun createFeedFragment(): FeedFragment = FeedFragment().apply {
    arguments = Bundle().apply {
        putInt(ARG_POST_ID, 70)
    }
}

fun createFeedFragment(userId: Int, postId: Int = 0, withToolbar: Boolean): FeedFragment =
    FeedFragment().apply {
        arguments = Bundle().apply {
            putInt(ARG_USER_ID, userId)
            putInt(ARG_POST_ID, postId)
            putBoolean(ARG_WITH_TOOLBAR, withToolbar)
        }
    }

private const val KEY_PRESENTER_STATE = "presenter_state"
private const val ARG_USER_ID = "user_id"
private const val ARG_POST_ID = "post_id"
private const val ARG_WITH_TOOLBAR = "with_toolbar"
