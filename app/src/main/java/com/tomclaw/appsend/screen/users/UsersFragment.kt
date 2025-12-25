package com.tomclaw.appsend.screen.users

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
import com.tomclaw.appsend.screen.profile.createProfileActivityIntent
import com.tomclaw.appsend.screen.users.di.SubscribersModule
import com.tomclaw.appsend.util.Analytics
import javax.inject.Inject

class UsersFragment : Fragment(), UsersPresenter.SubscribersRouter {

    @Inject
    lateinit var presenter: UsersPresenter

    @Inject
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    @Inject
    lateinit var analytics: Analytics

    override fun onCreate(savedInstanceState: Bundle?) {
        val context = context ?: return
        val userId = arguments?.getInt(ARG_USER_ID)
            ?: throw IllegalArgumentException("User ID must be provided")
        val name = arguments?.getString(ARG_USERS_TYPE)
            ?: throw IllegalArgumentException("User ID must be provided")
        val type = UsersType.valueOf(name)

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .subscribersComponent(SubscribersModule(context, type, userId, presenterState))
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
        val subscribersView = UsersViewImpl(view, adapter)

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
        activity?.onBackPressedDispatcher?.onBackPressed()
    }

}

fun createUsersFragment(
    userId: Int,
    type: UsersType,
): UsersFragment = UsersFragment().apply {
    arguments = Bundle().apply {
        putInt(ARG_USER_ID, userId)
        putString(ARG_USERS_TYPE, type.name)
    }
}

enum class UsersType {
    SUBSCRIBERS,
    PUBLISHERS,
}

private const val KEY_PRESENTER_STATE = "presenter_state"
private const val ARG_USER_ID = "user_id"
private const val ARG_USERS_TYPE = "users_type"
