package com.tomclaw.appsend.screen.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.TopicEntity
import com.tomclaw.appsend.screen.auth.request_code.createRequestCodeActivityIntent
import com.tomclaw.appsend.screen.chat.di.ChatModule
import com.tomclaw.appsend.screen.details.createDetailsActivityIntent
import com.tomclaw.appsend.screen.profile.createProfileActivityIntent
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.ZipParcelable
import com.tomclaw.appsend.util.getParcelableCompat
import com.tomclaw.appsend.util.updateTheme
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

    @Inject
    lateinit var analytics: Analytics

    override fun onCreate(savedInstanceState: Bundle?) {
        val topicEntity = intent.getParcelableExtra<TopicEntity>(EXTRA_TOPIC_ENTITY)
        val viewTitle = intent.getStringExtra(EXTRA_TITLE).takeIf { !it.isNullOrEmpty() }
            ?: getString(R.string.chat_activity)
        val topicId = intent.getIntExtra(EXTRA_TOPIC_ID, 0).takeIf { it != 0 }
            ?: topicEntity?.topicId ?: throw IllegalArgumentException("Topic ID must be provided")

        val compressedPresenterState: ZipParcelable? =
            savedInstanceState?.getParcelableCompat(KEY_PRESENTER_STATE, ZipParcelable::class.java)
        val presenterState: Bundle? = compressedPresenterState?.restore()
        appComponent
            .chatComponent(ChatModule(this, topicEntity, topicId, presenterState))
            .inject(activity = this)
        updateTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity)

        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val view =
            ChatViewImpl(window.decorView, preferences, adapter).apply { setTitle(viewTitle) }

        presenter.attachView(view)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-chat-screen")
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

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_PRESENTER_STATE, ZipParcelable(presenter.saveState()))
    }

    override fun openProfileScreen(userId: Int) {
        val intent = createProfileActivityIntent(this, userId)
        startActivity(intent)
    }

    override fun openAppScreen(packageName: String, title: String) {
        val intent = createDetailsActivityIntent(
            context = this,
            packageName = packageName,
            label = title,
            moderation = false,
            finishOnly = true
        )
        startActivity(intent)
    }

    override fun openLoginScreen() {
        val intent = createRequestCodeActivityIntent(context = this)
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

fun createChatActivityIntent(
    context: Context,
    topicEntity: TopicEntity,
): Intent = Intent(context, ChatActivity::class.java)
    .putExtra(EXTRA_TOPIC_ENTITY, topicEntity)

private const val EXTRA_TOPIC_ID = "topic_id"
private const val EXTRA_TITLE = "title"
private const val EXTRA_TOPIC_ENTITY = "topic_entity"
private const val KEY_PRESENTER_STATE = "presenter_state"
