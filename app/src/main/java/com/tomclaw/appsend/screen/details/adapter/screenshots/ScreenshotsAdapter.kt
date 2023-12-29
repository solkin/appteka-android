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
    private val items: MutableList<Screenshot> = mutableListOf()
) : RecyclerView.Adapter<ScreenshotViewHolder>() {

    private lateinit var listener: (Screenshot) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScreenshotViewHolder {
        val item = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.details_block_screenshot_item, parent, false)
        return ScreenshotViewHolder(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ScreenshotViewHolder, position: Int) {
        holder.setScreenshot(items[position])
        holder.setClickListener(listener)
    }

    override fun getItemViewType(position: Int): Int {
        return SCREENSHOT
    }

    public fun setItems(list: List<Screenshot>) {
        with(items) {
            clear()
            addAll(list)
        }
    }

    fun setOnItemClickListener(listener: (Screenshot) -> Unit) {
        this.listener = listener
    }

}

class ScreenshotViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private var image: ImageView
    private var screenshot: Screenshot? = null

    init {
        image = view.findViewById(R.id.screenshot)
    }

    fun setScreenshot(screenshot: Screenshot) {
        this.screenshot = screenshot

        val aspectRatio = screenshot.width.toFloat() / screenshot.height.toFloat()
        val width = image.layoutParams.height * aspectRatio
        image.layoutParams.width = width.toInt()

        image.fetch(screenshot.url) {
            centerCrop()
            placeholder = {
                with(it.get()) {
                    setImageDrawable(null)
                }
            }
        }
    }

    fun setClickListener(listener: (Screenshot) -> Unit) {
        val screenshot = screenshot ?: return
        image.setOnClickListener { listener.invoke(screenshot) }
    }

}

private const val SCREENSHOT: Int = 1
