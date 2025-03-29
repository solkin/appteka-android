package com.tomclaw.appsend.screen.feed

import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.auth.request_code.createRequestCodeActivityIntent
import com.tomclaw.appsend.screen.details.createDetailsActivityIntent
import com.tomclaw.appsend.screen.feed.di.FeedModule
import com.tomclaw.appsend.screen.gallery.GalleryItem
import com.tomclaw.appsend.screen.gallery.createGalleryActivityIntent
import com.tomclaw.appsend.screen.profile.createProfileActivityIntent
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.ZipParcelable
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

    private val authLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                presenter.invalidate()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val userId = arguments?.getInt(ARG_USER_ID)
        val postId = arguments?.getInt(ARG_POST_ID)
        val withToolbar = arguments?.getBoolean(ARG_WITH_TOOLBAR, false)

        val compressedPresenterState: ZipParcelable? = savedInstanceState?.getParcelable(KEY_PRESENTER_STATE, ZipParcelable::class.java)
        val presenterState: Bundle? = compressedPresenterState?.restore()
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
        outState.putParcelable(KEY_PRESENTER_STATE, ZipParcelable(presenter.saveState()))
    }

    override fun openProfileScreen(userId: Int) {
        val context = context ?: return
        val intent = createProfileActivityIntent(context, userId)
        startActivity(intent)
    }

    override fun openDetailsScreen(appId: String, label: String?, isFinish: Boolean) {
        val context = context ?: return
        val intent = createDetailsActivityIntent(
            context = context,
            appId = appId,
            label = label.orEmpty(),
            finishOnly = true
        )
        if (isFinish) {
            intent.flags = FLAG_ACTIVITY_CLEAR_TOP
            leaveScreen()
        }
        startActivity(intent)
    }

    override fun openGallery(items: List<GalleryItem>, current: Int) {
        val context = context ?: return
        val intent = createGalleryActivityIntent(context, items, current)
        startActivity(intent)
    }

    override fun openLoginScreen() {
        val context = context ?: return
        val intent = createRequestCodeActivityIntent(context)
        authLauncher.launch(intent)
    }

    override fun leaveScreen() {
        activity?.onBackPressed()
    }

}

fun createFeedFragment(): FeedFragment = FeedFragment()

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
