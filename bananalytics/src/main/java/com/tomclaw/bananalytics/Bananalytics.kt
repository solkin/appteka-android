package com.tomclaw.bananalytics

import com.tomclaw.bananalytics.api.BreadcrumbCategory

/**
 * Bananalytics - lightweight analytics and crash reporting library.
 */
interface Bananalytics {

    // === Lifecycle ===

    /**
     * Installs crash handler and sends pending crashes/events.
     * Should be called early in Application.onCreate().
     */
    fun install()

    // === Events ===

    fun trackEvent(name: String)

    fun trackEvent(name: String, key: String, value: String)

    fun trackEvent(name: String, key: String, value: Double)

    fun trackEvent(
        name: String,
        tags: Map<String, String> = emptyMap(),
        fields: Map<String, Double> = emptyMap()
    )

    /**
     * Flushes pending events to the backend.
     */
    fun flushEvents()

    // === Crashes ===

    /**
     * Tracks a non-fatal exception (caught exception that was handled).
     */
    fun trackException(throwable: Throwable, context: Map<String, String> = emptyMap())

    // === Breadcrumbs ===

    /**
     * Leaves a breadcrumb for crash context.
     */
    fun leaveBreadcrumb(message: String, category: BreadcrumbCategory = BreadcrumbCategory.CUSTOM)
}
