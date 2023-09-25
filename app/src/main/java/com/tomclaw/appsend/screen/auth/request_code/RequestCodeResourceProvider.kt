package com.tomclaw.appsend.screen.auth.request_code

import android.content.res.Resources
import com.tomclaw.appsend.R

interface RequestCodeResourceProvider {

    fun getRateLimitError(): String

    fun getCommonRequestCodeError(): String

    fun getNetworkError(): String

}

class RequestCodeResourceProviderImpl(val resources: Resources) : RequestCodeResourceProvider {

    override fun getRateLimitError(): String {
        return resources.getString(R.string.error_rate_limit)
    }

    override fun getCommonRequestCodeError(): String {
        return resources.getString(R.string.error_sending_request_code)
    }

    override fun getNetworkError(): String {
        return resources.getString(R.string.network_error)
    }

}
