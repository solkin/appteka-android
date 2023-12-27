package com.tomclaw.appsend.screen.details.adapter.screenshots

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.tomclaw.appsend.R
import com.tomclaw.imageloader.util.centerCrop
import com.tomclaw.imageloader.util.fetch

class ScreenshotsAdapter(
    private val urls: MutableList<String> = mutableListOf()
) : RecyclerView.Adapter<ScreenshotViewHolder>() {

    private lateinit var listener: (String) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScreenshotViewHolder {
        val item = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.details_block_screenshot_item, parent)
        return ScreenshotViewHolder(item)
    }

    override fun getItemCount(): Int {
        return urls.size
    }

    override fun onBindViewHolder(holder: ScreenshotViewHolder, position: Int) {
        holder.setUrl(urls[position])
        holder.setClickListener(listener)
    }

    override fun getItemViewType(position: Int): Int {
        return SCREENSHOT
    }

    public fun setUrls(list: List<String>) {
        with(urls) {
            clear()
            addAll(list)
        }
    }

    fun setOnItemClickListener(listener: (String) -> Unit) {
        this.listener = listener
    }

}

class ScreenshotViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private var image: ImageView
    private var url: String

    init {
        image = view.findViewById(R.id.screenshot)
        url = ""
    }

    fun setUrl(url: String) {
        this.url = url
        image.fetch(url) {
            centerCrop()
            placeholder = {
                with(it.get()) {
                    setImageDrawable(null)
                }
            }
        }
    }

    fun setClickListener(listener: (String) -> Unit) {
        image.setOnClickListener { listener.invoke(url) }
    }

}

private const val SCREENSHOT: Int = 1
