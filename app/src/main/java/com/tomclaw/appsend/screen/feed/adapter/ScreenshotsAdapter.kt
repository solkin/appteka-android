package com.tomclaw.appsend.screen.feed.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.Screenshot
import com.tomclaw.imageloader.util.fetch

class ScreenshotsAdapter(
    private val listener: ItemListener
) : RecyclerView.Adapter<ScreenshotsAdapter.ViewHolder>() {

    val dataSet = ArrayList<Screenshot>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val card: View = view.findViewById(R.id.screenshot_card)
        private val image: ImageView = view.findViewById(R.id.screenshot)

        private var clickListener: (() -> Unit)? = null

        init {
            card.setOnClickListener { clickListener?.invoke() }
        }

        fun setImage(item: Screenshot) {
            val aspectRatio = item.width.toFloat() / item.height.toFloat()
            val width = image.layoutParams.height * aspectRatio
            image.layoutParams.width = width.toInt()

            image.fetch(item.preview) {
                centerCrop()
                onLoading { imageView ->
                    imageView.setImageDrawable(null)
                }
            }
        }

        fun setOnClickListener(listener: (() -> Unit)?) {
            this.clickListener = listener
        }

        fun onUnbind() {
            this.clickListener = null
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.feed_screenshot_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = dataSet[position]
        with(viewHolder) {
            setImage(item)
            setOnClickListener { listener.onImageClick(dataSet, position) }
        }
    }

    override fun getItemCount() = dataSet.size

}
