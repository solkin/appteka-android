package com.tomclaw.appsend.screen.subscribers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.fragment.app.Fragment
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.details.createDetailsActivityIntent
import com.tomclaw.appsend.screen.profile.createProfileActivityIntent
import com.tomclaw.appsend.screen.subscribers.di.SubscribersModule
import com.tomclaw.appsend.util.Analytics
import javax.inject.Inject

class SubscribersFragment : Fragment(), SubscribersPresenter.SubscribersRouter {

    @Inject
    lateinit var presenter: SubscribersPresenter

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
        val context = context ?: return
        val userId = arguments?.getInt(ARG_USER_ID)
            ?: throw IllegalArgumentException("User ID must be provided")

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .subscribersComponent(SubscribersModule(context, userId, presenterState))
            .inject(fragment = this)

        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-subscribers-fragment")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.subscribers_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val subscribersView = SubscribersViewImpl(view, adapter)

        presenter.attachView(subscribersView)
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

    override fun leaveScreen() {
        activity?.onBackPressed()
    }

}

fun createSubscribersFragment(
    userId: Int,
): SubscribersFragment = SubscribersFragment().apply {
    arguments = Bundle().apply {
        putInt(ARG_USER_ID, userId)
    }
}

private const val KEY_PRESENTER_STATE = "presenter_state"
private const val ARG_USER_ID = "user_id"
