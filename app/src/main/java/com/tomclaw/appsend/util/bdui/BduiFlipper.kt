package com.tomclaw.appsend.util.bdui

import android.content.Context
import android.widget.ViewFlipper

/** ViewFlipper's getter is unavailable before API 29; every write still uses its setter. */
class BduiFlipper @JvmOverloads constructor(
    context: Context,
    interval: Int = DEFAULT_INTERVAL,
) : ViewFlipper(context) {

    var currentInterval: Int = interval
        private set

    init {
        setFlipInterval(interval)
    }

    override fun setFlipInterval(milliseconds: Int) {
        super.setFlipInterval(milliseconds)
        currentInterval = milliseconds
    }

    companion object {
        const val DEFAULT_INTERVAL = 3000
    }
}
