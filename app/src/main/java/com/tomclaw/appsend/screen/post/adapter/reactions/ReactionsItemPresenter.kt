package com.tomclaw.appsend.screen.post.adapter.reactions

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.post.adapter.ItemListener

class ReactionsItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<ReactionsItemView, ReactionsItem> {

    override fun bindView(view: ReactionsItemView, item: ReactionsItem, position: Int) {
        view.setOnReactionClickListener { reaction ->
            listener.onReactionClick(reaction)
        }
        view.setReactions(item.availableReactions, item.selectedReactionIds)
    }

}

