package com.tomclaw.appsend.screen.details.adapter.security

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.details.adapter.ItemListener

class SecurityItemPresenter(
    private val listener: ItemListener
) : ItemPresenter<SecurityItemView, SecurityItem> {

    override fun bindView(view: SecurityItemView, item: SecurityItem, position: Int) {
        with(view) {
            when (item.type) {
                SecurityType.NOT_SCANNED -> setSecurityTypeNotScanned()
                SecurityType.SCANNING -> setSecurityTypeScanning()
                SecurityType.SAFE -> setSecurityTypeSafe()
                SecurityType.SUSPICIOUS -> setSecurityTypeSuspicious()
                SecurityType.MALWARE -> setSecurityTypeMalware()
                SecurityType.UNKNOWN -> setSecurityTypeUnknown()
            }
            if (item.showAction) {
                showActionButton(item.actionLabel.orEmpty())
                setOnActionClickListener { listener.onRequestSecurityScan(item.appId) }
            } else {
                hideActionButton()
            }
            setSecurityText(item.text)
        }
    }

}

