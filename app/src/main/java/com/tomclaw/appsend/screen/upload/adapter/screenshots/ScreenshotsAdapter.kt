package com.tomclaw.appsend.screen.upload.adapter.screenshots

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.upload.adapter.screenshots.adapter.APPEND
import com.tomclaw.appsend.screen.upload.adapter.screenshots.adapter.IMAGE
import com.tomclaw.appsend.screen.upload.adapter.screenshots.adapter.Item
import com.tomclaw.appsend.screen.upload.adapter.screenshots.adapter.ScreenshotAppend
import com.tomclaw.appsend.screen.upload.adapter.screenshots.adapter.ScreenshotImage
import com.tomclaw.imageloader.util.centerCrop
import com.tomclaw.imageloader.util.fetch

class ScreenshotsAdapter(
    private val items: MutableList<Item> = mutableListOf()
) : RecyclerView.Adapter<ItemViewHolder<*>>() {

    private lateinit var listener: (Item) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder<*> {
        val item = LayoutInflater.from(parent.context)
            .inflate(R.layout.upload_block_screenshot_image_item, parent, false)
        return when (viewType) {
            APPEND -> AppendViewHolder(item)
            IMAGE -> ImageViewHolder(item)
            else -> throw IllegalArgumentException("Invalid viewType $viewType")
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder<*>, position: Int) {
        when (holder) {
            is AppendViewHolder -> {
                holder.setItem(items[position] as ScreenshotAppend)
                holder.setClickListener(listener)
            }

            is ImageViewHolder -> {
                holder.setItem(items[position] as ScreenshotImage)
                holder.setClickListener(listener)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].getType()
    }

    public fun setItems(list: List<Item>) {
        with(items) {
            clear()
            addAll(list)
        }
    }

    fun setOnItemClickListener(listener: (Item) -> Unit) {
        this.listener = listener
    }

}

abstract class ItemViewHolder<I : Item>(view: View) : RecyclerView.ViewHolder(view) {

    abstract fun setItem(item: I)

    abstract fun setClickListener(listener: (I) -> Unit)

}

class AppendViewHolder(view: View) : ItemViewHolder<ScreenshotAppend>(view) {

    override fun setItem(item: ScreenshotAppend) {
        TODO("Not yet implemented")
    }

    override fun setClickListener(listener: (ScreenshotAppend) -> Unit) {
        TODO("Not yet implemented")
    }

}

class ImageViewHolder(view: View) : ItemViewHolder<ScreenshotImage>(view) {

    private var image: ImageView
    private var screenshot: ScreenshotImage? = null

    init {
        image = view.findViewById(R.id.screenshot)
    }

    override fun setItem(item: ScreenshotImage) {
        this.screenshot = item

        val aspectRatio = item.width.toFloat() / item.height.toFloat()
        val width = image.layoutParams.height * aspectRatio
        image.layoutParams.width = width.toInt()

        image.fetch(item.uri.toString()) {
            centerCrop()
            placeholder = {
                with(it.get()) {
                    setImageDrawable(null)
                }
            }
        }
    }

    override fun setClickListener(listener: (ScreenshotImage) -> Unit) {
        val screenshot = screenshot ?: return
        image.setOnClickListener { listener.invoke(screenshot) }
    }

}
