package com.tomclaw.appsend.screen.chat.adapter.incoming

import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.chat.adapter.ItemListener
import java.util.Locale

class IncomingMsgItemPresenter(
    private val locale: Locale,
    private val listener: ItemListener,
) : ItemPresenter<IncomingMsgItemView, IncomingMsgItem> {

    override fun bindView(view: IncomingMsgItemView, item: IncomingMsgItem, position: Int) {
        val name = item.author.name.takeIf { !it.isNullOrBlank() }
            ?: item.author.icon?.label?.get(locale.language)
            ?: item.author.icon?.label?.get(DEFAULT_LOCALE)
        item.author.icon?.let(view::setUserIcon)
        view.setUserBadge(item.author.primaryBadge)
        view.setAuthor(name, item.author.icon?.color)
        view.setTime(item.time)
        view.setDate(item.date)
        view.setText(item.text)
        view.setAttachments(item.attachments)

        view.setOnClickListener { listener.onItemClick(item) }
        view.setOnAvatarClickListener { listener.onAvatarClick(item.author.id) }
        view.setOnAttachmentClickListener { index -> listener.onAttachmentClick(item, index) }
    }

}
