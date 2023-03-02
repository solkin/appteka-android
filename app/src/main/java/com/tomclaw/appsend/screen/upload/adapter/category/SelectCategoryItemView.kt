package com.tomclaw.appsend.screen.upload.adapter.category

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.categories.CategoryItem
import com.tomclaw.appsend.util.svgToDrawable

interface SelectCategoryItemView : ItemView {

    fun setSelectedCategory(category: CategoryItem?)

    fun setOnClickListener(listener: (() -> Unit)?)

}

@Suppress("DEPRECATION")
class SelectCategoryItemViewHolder(view: View) : BaseViewHolder(view), SelectCategoryItemView {

    private val context = view.context
    private val selectorView: View = view.findViewById(R.id.selector_back)
    private val categoryIcon: ImageView = view.findViewById(R.id.category_icon)
    private val categoryTitle: TextView = view.findViewById(R.id.category_title)

    private var clickListener: (() -> Unit)? = null

    init {
        selectorView.setOnClickListener { clickListener?.invoke() }
    }

    override fun setSelectedCategory(category: CategoryItem?) {
        category?.let {
            categoryIcon.setImageDrawable(svgToDrawable(it.icon, context.resources))
            categoryTitle.text = it.title
        } ?: run {
            categoryIcon.setImageResource(R.drawable.ic_category)
            categoryTitle.setText(R.string.category_not_defined)
        }
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
