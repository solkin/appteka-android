package com.tomclaw.appsend.screen.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.main.profile.ProfileActivity.createProfileActivityIntent
import com.tomclaw.appsend.screen.chat.di.ChatModule
import com.tomclaw.appsend.util.ThemeHelper
import javax.inject.Inject

class ChatActivity : AppCompatActivity(), ChatPresenter.ChatRouter {

    @Inject
    lateinit var presenter: ChatPresenter

    @Inject
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    @Inject
    lateinit var preferences: ChatPreferencesProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        val viewTitle = intent.getStringExtra(EXTRA_TITLE).takeIf { !it.isNullOrEmpty() }
            ?: getString(R.string.chat_activity)
        val topicId = intent.getIntExtra(EXTRA_TOPIC_ID, 0).takeIf { it != 0 }
            ?: throw IllegalArgumentException("Topic ID must be provided")

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .chatComponent(ChatModule(this, topicId, presenterState))
            .inject(activity = this)
        ThemeHelper.updateTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity)

        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val view =
            ChatViewImpl(window.decorView, preferences, adapter).apply { setTitle(viewTitle) }

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

    override fun openProfileScreen(userId: Int) {
        val intent = createProfileActivityIntent(this, userId)
        startActivity(intent)
    }

    override fun leaveScreen() {
        finish()
    }

}

fun createChatActivityIntent(
    context: Context,
    topicId: Int,
    title: String?,
): Intent = Intent(context, ChatActivity::class.java)
    .putExtra(EXTRA_TOPIC_ID, topicId)
    .putExtra(EXTRA_TITLE, title)

private const val EXTRA_TOPIC_ID = "topic_id"
private const val EXTRA_TITLE = "title"
private const val KEY_PRESENTER_STATE = "presenter_state"
