package com.tomclaw.appsend.screen.topics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.TopicEntity
import com.tomclaw.appsend.main.home.HomeFragment
import com.tomclaw.appsend.screen.auth.request_code.createRequestCodeActivityIntent
import com.tomclaw.appsend.screen.chat.createChatActivityIntent
import com.tomclaw.appsend.screen.topics.di.TopicsModule
import javax.inject.Inject

class TopicsFragment : HomeFragment(), TopicsPresenter.TopicsRouter {

    @Inject
    lateinit var presenter: TopicsPresenter

    @Inject
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var preferences: TopicsPreferencesProvider

    @Inject
    lateinit var binder: ItemBinder

    override fun onCreate(savedInstanceState: Bundle?) {
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .topicsComponent(TopicsModule(requireContext(), presenterState))
            .inject(fragment = this)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.topics_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val topicsView = TopicsViewImpl(view, preferences, adapter)

        presenter.attachView(topicsView)
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

    override fun showChatScreen(entity: TopicEntity) {
        val intent = createChatActivityIntent(requireContext(), entity)
        startActivity(intent)
    }

    override fun openLoginScreen() {
        val intent = createRequestCodeActivityIntent(requireContext())
        startActivity(intent)
    }

}

private const val KEY_PRESENTER_STATE = "presenter_state"
