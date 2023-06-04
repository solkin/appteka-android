package com.tomclaw.appsend.screen.upload.adapter.category

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.ItemListener

class SelectCategoryItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<SelectCategoryItemView, SelectCategoryItem> {

    override fun bindView(view: SelectCategoryItemView, item: SelectCategoryItem, position: Int) {
        with(view) {
            setSelectedCategory(item.category)
            if (item.errorRequiredField) {
                showRequiredFieldError()
            } else {
                hideRequiredFieldError()
            }
            setOnClickListener { listener.onCategoryClick() }
        }
    }

}
