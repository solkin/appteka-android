package com.tomclaw.appsend.categories

import java.util.Locale

interface CategoryConverter {

    fun convert(category: Category): CategoryItem

}

class CategoryConverterImpl(private val locale: Locale) : CategoryConverter {

    override fun convert(category: Category): CategoryItem {
        return CategoryItem(
            id = category.id,
            title = category.name[locale.language] ?: category.name[DEFAULT_LOCALE].orEmpty(),
            icon = category.icon
        )
    }

}

const val DEFAULT_LOCALE = "en"
