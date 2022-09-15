package com.tomclaw.appsend.screen.rate

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.clicks
import com.tomclaw.imageloader.util.centerCrop
import com.tomclaw.imageloader.util.fetch
import com.tomclaw.imageloader.util.withPlaceholder
import io.reactivex.rxjava3.core.Observable

interface RateView {

    fun setTitle(title: String)

    fun setIcon(url: String?)

    fun navigationClicks(): Observable<Unit>
}

class RateViewImpl(view: View) : RateView {

    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val back: View = view.findViewById(R.id.go_back)
    private val icon: ImageView = view.findViewById(R.id.icon)
    private val title: TextView = view.findViewById(R.id.title)
    private val subtitle: TextView = view.findViewById(R.id.subtitle)

    private val navigationRelay = PublishRelay.create<Unit>()

    init {
        subtitle.setText(R.string.rate_app)
        back.clicks(navigationRelay)
    }

    override fun setTitle(title: String) {
        this.title.bind(title)
    }

    override fun setIcon(url: String?) {
        icon.fetch(url.orEmpty()) {
            centerCrop()
            withPlaceholder(R.drawable.app_placeholder)
            placeholder = {
                with(it.get()) {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageResource(R.drawable.app_placeholder)
                }
            }
        }
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

}
