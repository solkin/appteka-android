package com.tomclaw.appsend.screen.details.adapter.ai_note

import com.tomclaw.appsend.screen.details.DetailsResourceProvider
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import com.tomclaw.appsend.util.adapter.ItemPresenter

class AINoteItemPresenter(
    private val listener: ItemListener,
    private val resourceProvider: DetailsResourceProvider,
) : ItemPresenter<AINoteItemView, AINoteItem> {

    override fun bindView(view: AINoteItemView, item: AINoteItem, position: Int) {
        when (item.state) {
            AINoteState.IDLE -> {
                view.renderIdle(resourceProvider.aiNoteIdlePrompt())
                view.setOnAskClickListener { listener.onRequestAIReview(item.appId) }
            }
            AINoteState.PENDING -> {
                view.renderPending(resourceProvider.aiNotePending())
                view.setOnAskClickListener(null)
            }
            AINoteState.COMPLETED -> {
                view.renderCompleted(item.note.orEmpty())
                view.setOnAskClickListener(null)
            }
        }
    }

}
