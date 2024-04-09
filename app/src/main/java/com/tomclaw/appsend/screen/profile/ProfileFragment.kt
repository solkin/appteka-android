package com.tomclaw.appsend.screen.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.main.home.HomeFragment
import com.tomclaw.appsend.main.profile.list.FilesActivity.createUserAppsActivityIntent
import com.tomclaw.appsend.screen.auth.request_code.createRequestCodeActivityIntent
import com.tomclaw.appsend.screen.details.createDetailsActivityIntent
import com.tomclaw.appsend.screen.favorite.createFavoriteActivityIntent
import com.tomclaw.appsend.screen.profile.di.PROFILE_ADAPTER_PRESENTER
import com.tomclaw.appsend.screen.profile.di.ProfileModule
import com.tomclaw.appsend.screen.reviews.createReviewsActivityIntent
import javax.inject.Inject
import javax.inject.Named

class ProfileFragment : HomeFragment(), ProfilePresenter.ProfileRouter {

    @Inject
    lateinit var presenter: ProfilePresenter

    @Inject
    @Named(PROFILE_ADAPTER_PRESENTER)
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    private val authLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                presenter.onAuthorized()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val userId = arguments?.getInt(ARG_USER_ID)
        val withToolbar = arguments?.getBoolean(ARG_WITH_TOOLBAR, true)

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .profileComponent(ProfileModule(userId, withToolbar, presenterState))
            .inject(fragment = this)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.profile_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val profileView = ProfileViewImpl(view, adapter)

        presenter.attachView(profileView)
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

    override fun openUserFilesScreen(userId: Int) {
        val context = context ?: return
        val intent = createUserAppsActivityIntent(context, userId)
        startActivity(intent)
    }

    override fun openDetailsScreen(appId: String, label: String?) {
        val context = context ?: return
        val intent = createDetailsActivityIntent(
            context,
            appId,
            label = label.orEmpty(),
            moderation = false,
            finishOnly = true
        )
        startActivity(intent)
    }

    override fun openFavoriteScreen(userId: Int) {
        val context = context ?: return
        val intent = createFavoriteActivityIntent(context, userId)
        startActivity(intent)
    }

    override fun openReviewsScreen(userId: Int) {
        val context = context ?: return
        val intent = createReviewsActivityIntent(context, userId)
        startActivity(intent)
    }

    override fun openShareUrlDialog(text: String) {
        val intent = Intent().apply {
            setAction(Intent.ACTION_SEND)
            putExtra(Intent.EXTRA_TEXT, text)
            setType("text/plain")
        }
        val chooser = Intent.createChooser(intent, resources.getText(R.string.send_url_to))
        startActivity(chooser)
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

fun createProfileFragment(
    userId: Int,
    withToolbar: Boolean = true,
): ProfileFragment = ProfileFragment().apply {
    arguments = Bundle().apply {
        putInt(ARG_USER_ID, userId)
        putBoolean(ARG_WITH_TOOLBAR, withToolbar)
    }
}

private const val KEY_PRESENTER_STATE = "presenter_state"
private const val ARG_USER_ID = "user_id"
private const val ARG_WITH_TOOLBAR = "with_toolbar"
