package com.tomclaw.bananalytics

import com.tomclaw.bananalytics.api.Environment

/**
 * Provider for environment information.
 * Implementations should be provided by the app module.
 */
interface EnvironmentProvider {

    fun environment(): Environment
}
