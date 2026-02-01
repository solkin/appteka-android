package com.tomclaw.appsend.screen.upload.adapter.notice

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.ItemListener

class NoticeItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<NoticeItemView, NoticeItem> {

    override fun bindView(view: NoticeItemView, item: NoticeItem, position: Int) {
        with(view) {
            when (item.type) {
                NoticeType.INFO -> setNoticeTypeInfo()
                NoticeType.WARNING -> setNoticeTypeWarning()
                NoticeType.ERROR -> setNoticeTypeError()
            }
            setNoticeText(item.text)
            val listener = if (item.clickable) {
                { listener.onNoticeClick() }
            } else {
                null
            }
            setOnClickListener(listener)
        }
    }

}
