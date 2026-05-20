package com.tomclaw.appsend.screen.details.adapter.ai_note

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show

interface AINoteItemView : ItemView {

    fun renderIdle(prompt: String)

    fun renderPending(pendingCaption: String)

    fun renderCompleted(note: String)

    fun setOnAskClickListener(listener: (() -> Unit)?)

}

class AINoteItemViewHolder(view: View) : BaseItemViewHolder(view), AINoteItemView {

    private val body: TextView = view.findViewById(R.id.ai_note_body)
    private val pendingContainer: FrameLayout = view.findViewById(R.id.ai_note_pending_container)
    private val askButton: MaterialButton = view.findViewById(R.id.ai_note_ask_button)

    private var askClickListener: (() -> Unit)? = null

    init {
        askButton.setOnClickListener { askClickListener?.invoke() }
    }

    override fun renderIdle(prompt: String) {
        body.bind(prompt)
        body.show()
        pendingContainer.hide()
        askButton.show()
    }

    override fun renderPending(pendingCaption: String) {
        body.bind(pendingCaption)
        body.show()
        pendingContainer.show()
        askButton.hide()
    }

    override fun renderCompleted(note: String) {
        body.bind(note)
        body.show()
        pendingContainer.hide()
        askButton.hide()
    }

    override fun setOnAskClickListener(listener: (() -> Unit)?) {
        this.askClickListener = listener
    }

    override fun onUnbind() {
        this.askClickListener = null
    }

}
