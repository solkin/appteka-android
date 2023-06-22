package com.tomclaw.appsend.screen.details.adapter.status

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.details.adapter.ItemListener

class StatusItemPresenter(
    private val listener: ItemListener
) : ItemPresenter<StatusItemView, StatusItem> {

    override fun bindView(view: StatusItemView, item: StatusItem, position: Int) {
        with(view) {
            when (item.type) {
                StatusType.INFO -> setStatusTypeInfo()
                StatusType.WARNING -> setStatusTypeWarning()
                StatusType.ERROR -> setStatusTypeError()
            }
            when (item.actionType) {
                StatusAction.NONE -> hideActionButton()
                else -> {
                    showActionButton(item.actionLabel.orEmpty())
                    setOnActionClickListener { listener.onStatusAction(item.actionType) }
                }
            }
            setStatusText(item.text)
        }
    }

}
