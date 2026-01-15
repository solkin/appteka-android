package com.tomclaw.appsend.screen.details.adapter.abi

import com.avito.konveyor.blueprint.ItemPresenter

class AbiItemPresenter(
    private val resourceProvider: AbiResourceProvider,
) : ItemPresenter<AbiItemView, AbiItem> {

    override fun bindView(view: AbiItemView, item: AbiItem, position: Int) {
        val formattedAbis = item.abiList.map { resourceProvider.formatAbiName(it) }
        val compatibilityText = resourceProvider.getCompatibilityText(item.isCompatible)

        view.showArchitectures(formattedAbis)
        view.showCompatibility(compatibilityText, item.isCompatible)
    }

}
