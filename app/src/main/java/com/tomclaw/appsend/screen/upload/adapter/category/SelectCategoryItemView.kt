package com.tomclaw.appsend.screen.upload.adapter.category

import android.view.View
import android.widget.AutoCompleteTextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.google.android.material.textfield.TextInputLayout
import com.tomclaw.appsend.R
import com.tomclaw.appsend.categories.CategoryItem
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.util.svgToDrawable

interface SelectCategoryItemView : ItemView {

    fun setSelectedCategory(category: CategoryItem?)

    fun showRequiredFieldError()

    fun hideRequiredFieldError()

    fun setOnClickListener(listener: (() -> Unit)?)

}

class SelectCategoryItemViewHolder(view: View) : BaseViewHolder(view), SelectCategoryItemView {

    private val context = view.context
    private val selectorLayout: TextInputLayout = view.findViewById(R.id.selector_back)
    private val categoryDropdown: AutoCompleteTextView = view.findViewById(R.id.category_dropdown)
    private val requiredFieldErrorView: View = view.findViewById(R.id.required_field_error)

    private var clickListener: (() -> Unit)? = null

    init {
        categoryDropdown.setOnClickListener { clickListener?.invoke() }
        categoryDropdown.isFocusable = false
        categoryDropdown.isFocusableInTouchMode = false
    }

    override fun setSelectedCategory(category: CategoryItem?) {
        category?.let {
            val icon = svgToDrawable(it.icon, context.resources)
            selectorLayout.startIconDrawable = icon
            categoryDropdown.setText(it.title, false)
        } ?: run {
            selectorLayout.setStartIconDrawable(R.drawable.ic_category)
            categoryDropdown.setText(context.getString(R.string.category_not_defined), false)
        }
    }

    override fun showRequiredFieldError() {
        requiredFieldErrorView.show()
        selectorLayout.error = " "
        selectorLayout.isErrorEnabled = true
    }

    override fun hideRequiredFieldError() {
        requiredFieldErrorView.hide()
        selectorLayout.error = null
        selectorLayout.isErrorEnabled = false
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
