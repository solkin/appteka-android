package com.tomclaw.appsend.screen.rate

import android.content.res.Resources
import com.tomclaw.appsend.R

interface RateResourceProvider {

    fun getReviewRequiredError(): String

    fun getRatingRequiredError(): String

}

class RateResourceProviderImpl(
    private val resources: Resources
) : RateResourceProvider {

    override fun getReviewRequiredError(): String {
        return resources.getString(R.string.review_required_error)
    }

    override fun getRatingRequiredError(): String {
        return resources.getString(R.string.rating_required_error)
    }

}

