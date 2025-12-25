package com.tomclaw.appsend.screen.store

import android.content.res.Resources
import com.tomclaw.appsend.R
import com.tomclaw.appsend.categories.CategoryItem

interface CategoryDropdownItemConverter {

    fun convert(items: List<CategoryItem>): List<CategoryDropdownItem>

}

class CategoryDropdownItemConverterImpl(
    private val resources: Resources
) : CategoryDropdownItemConverter {

    override fun convert(items: List<CategoryItem>): List<CategoryDropdownItem> {
        val allCategoriesItem = CategoryDropdownItem(
            id = 0,
            title = resources.getString(R.string.all_categories),
            iconSvg = null,
            iconRes = R.drawable.ic_category
        )
        return listOf(allCategoriesItem) + items.map { item ->
            CategoryDropdownItem(
                id = item.id,
                title = item.title,
                iconSvg = item.icon,
                iconRes = 0
            )
        }
    }

}

