package com.tomclaw.appsend.screen.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.main.home.HomeFragment
import com.tomclaw.appsend.screen.profile.di.ProfileModule
import javax.inject.Inject

class ProfileFragment : HomeFragment(), ProfilePresenter.ProfileRouter {

    @Inject
    lateinit var presenter: ProfilePresenter

    @Inject
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    override fun onCreate(savedInstanceState: Bundle?) {
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .profileComponent(ProfileModule(requireContext(), presenterState))
            .inject(fragment = this)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.profile_fragment_legacy, container, false)
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

}

private const val KEY_PRESENTER_STATE = "presenter_state"
