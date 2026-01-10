package com.tomclaw.bananalytics.internal

import com.tomclaw.bananalytics.api.Breadcrumb
import com.tomclaw.bananalytics.api.BreadcrumbCategory

internal class BreadcrumbBuffer(private val maxSize: Int = DEFAULT_MAX_SIZE) {

    private val buffer = ArrayDeque<Breadcrumb>(maxSize)

    @Synchronized
    fun add(message: String, category: BreadcrumbCategory) {
        if (buffer.size >= maxSize) {
            buffer.removeFirst()
        }
        buffer.addLast(
            Breadcrumb(
                timestamp = System.currentTimeMillis(),
                message = message,
                category = category.toApiValue()
            )
        )
    }

    @Synchronized
    fun snapshot(): List<Breadcrumb> = buffer.toList()

    @Synchronized
    fun clear() {
        buffer.clear()
    }

    companion object {
        private const val DEFAULT_MAX_SIZE = 50
    }
}
