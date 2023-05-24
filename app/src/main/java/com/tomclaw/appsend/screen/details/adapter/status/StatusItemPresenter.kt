package com.tomclaw.appsend.screen.details.adapter.status

import com.avito.konveyor.blueprint.ItemPresenter

class StatusItemPresenter : ItemPresenter<StatusItemView, StatusItem> {

    override fun bindView(view: StatusItemView, item: StatusItem, position: Int) {
        with(view) {
            when (item.type) {
                StatusType.INFO -> setStatusTypeInfo()
                StatusType.WARNING -> setStatusTypeWarning()
                StatusType.ERROR -> setStatusTypeError()
            }
            setStatusText("")
        }
    }

}
