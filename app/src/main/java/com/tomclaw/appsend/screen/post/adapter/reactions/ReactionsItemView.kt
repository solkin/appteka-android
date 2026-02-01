package com.tomclaw.appsend.screen.post.adapter.reactions

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.feed.adapter.ReactionsAdapter
import com.tomclaw.appsend.screen.feed.api.Reaction
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show

interface ReactionsItemView : ItemView {

    fun setReactions(
        availableReactions: List<Reaction>,
        selectedReactionIds: Set<String>
    )

    fun setOnReactionClickListener(listener: ((Reaction) -> Unit)?)

}

class ReactionsItemViewHolder(
    view: View,
    val reactionsAdapter: ReactionsAdapter,
) : BaseItemViewHolder(view), ReactionsItemView {

    private val reactions: RecyclerView = view.findViewById(R.id.reactions)

    init {
        val orientation = RecyclerView.HORIZONTAL
        val layoutManager = LinearLayoutManager(view.context, orientation, false)
        reactions.adapter = reactionsAdapter
        reactions.layoutManager = layoutManager
        reactions.itemAnimator = DefaultItemAnimator()
        reactions.itemAnimator?.changeDuration = 300L
    }

    override fun setOnReactionClickListener(listener: ((Reaction) -> Unit)?) {
        reactionsAdapter.setClickListener(listener)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun setReactions(
        availableReactions: List<Reaction>,
        selectedReactionIds: Set<String>
    ) {
        if (availableReactions.isNotEmpty()) {
            reactions.show()
            // Обновляем данные в адаптере, помечая выбранные реакции
            reactionsAdapter.dataSet.clear()
            reactionsAdapter.dataSet.addAll(
                availableReactions.map { reaction ->
                    reaction.copy(active = selectedReactionIds.contains(reaction.id))
                }
            )
            reactionsAdapter.notifyDataSetChanged()
        } else {
            reactions.hide()
        }
    }

    override fun onUnbind() {
        reactionsAdapter.setClickListener(null)
    }

}
