package com.tomclaw.appsend.util

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import android.widget.ImageView
import com.tomclaw.imageloader.core.ViewHolder
import com.tomclaw.imageloader.core.ViewSize
import java.util.concurrent.CountDownLatch

class ImageViewHolder(private val imageView: ImageView) : ViewHolder<ImageView> {

    override fun getSize(): ViewSize {
        optSize()?.let { return it }

        var viewSize = ViewSize(0, 0)
        val latch = CountDownLatch(1)

        val preDrawListener = object : ViewTreeObserver.OnPreDrawListener {
            private var isResumed = false

            override fun onPreDraw(): Boolean {
                val size = optSize()
                if (size != null) {
                    viewSize = size
                    imageView.viewTreeObserver.removeOnPreDrawListener(this)

                    if (!isResumed) {
                        isResumed = true
                        latch.countDown()
                    }
                }
                return true
            }
        }
        imageView.viewTreeObserver.addOnPreDrawListener(preDrawListener)

        latch.await()
        return viewSize
    }

    override fun optSize(): ViewSize? {
        val width = (
                imageView.layoutParams?.width?.takeIf { it > 0 }
                    ?: imageView.width.takeIf { it > 0 }
                    ?: MATCH_PARENT
                )
            .takeIf { it != WRAP_CONTENT }
            ?: imageView.context.resources.displayMetrics.widthPixels
        val height = (
                imageView.layoutParams?.height?.takeIf { it > 0 }
                    ?: imageView.height.takeIf { it > 0 }
                    ?: MATCH_PARENT
                )
            .takeIf { it != WRAP_CONTENT }
            ?: imageView.context.resources.displayMetrics.heightPixels
        return ViewSize(width, height).takeIf { width > 0 && height > 0 }
    }

    override var tag: Any?
        get() = imageView.tag
        set(value) {
            imageView.tag = value
        }

    override fun get(): ImageView {
        return imageView
    }

}
