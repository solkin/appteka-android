package com.tomclaw.appsend.screen.store

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.categories.CategoryItem
import com.tomclaw.appsend.util.svgToDrawable

data class CategoryDropdownItem(
    val id: Int,
    val title: String,
    val iconSvg: String?,
    val iconRes: Int
)

class CategoryDropdownAdapter(
    context: Context,
    private val items: List<CategoryDropdownItem>
) : ArrayAdapter<CategoryDropdownItem>(context, R.layout.dropdown_item, items) {

    private val inflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.dropdown_item, parent, false)
        val item = getItem(position) ?: return view

        val icon = view.findViewById<ImageView>(R.id.dropdown_item_icon)
        val text = view.findViewById<TextView>(R.id.dropdown_item_text)

        text.text = item.title

        if (item.iconSvg != null) {
            icon.setImageDrawable(svgToDrawable(item.iconSvg, context.resources))
        } else if (item.iconRes != 0) {
            icon.setImageResource(item.iconRes)
        }

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                return FilterResults().apply {
                    values = items
                    count = items.size
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                notifyDataSetChanged()
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as? CategoryDropdownItem)?.title ?: ""
            }
        }
    }
}

