package com.tomclaw.appsend.screen.details.adapter.description

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.details.TRANSLATION_ORIGINAL
import com.tomclaw.appsend.screen.details.TRANSLATION_PROGRESS
import com.tomclaw.appsend.screen.details.TRANSLATION_TRANSLATED
import com.tomclaw.appsend.screen.details.adapter.ItemListener

class DescriptionItemPresenter(
    private val listener: ItemListener,
    private val resourceProvider: DescriptionResourceProvider
) : ItemPresenter<DescriptionItemView, DescriptionItem> {

    override fun bindView(view: DescriptionItemView, item: DescriptionItem, position: Int) {
        view.setText(item.text)
        view.setAppVersion(resourceProvider.formatFileVersion(item.versionName, item.versionCode))
        view.setVersionsCount(item.versionsCount)
        view.setUploadDate(resourceProvider.formatDate(item.uploadDate))
        view.setChecksum(item.checksum)
        view.setSourceUrl(item.sourceUrl)
        when (item.translationState) {
            TRANSLATION_ORIGINAL -> {
                view.enableTranslateButton()
                view.showTranslateButton()
            }

            TRANSLATION_TRANSLATED -> {
                view.enableTranslateButton()
                view.showOriginalButton()
            }

            TRANSLATION_PROGRESS -> view.disableTranslateButton()
        }

        view.setOnTranslateClickListener { listener.onTranslateClick() }
        view.setOnGooglePlayClickListener { listener.onGooglePlayClick() }
        view.setOnVersionsClickListener { listener.onVersionsClick() }
    }

}
