package com.tomclaw.appsend.screen.auth.verify_code

import android.content.res.Resources
import com.tomclaw.appsend.R

interface VerifyCodeResourceProvider {

    fun formatCodeSentDescription(email: String): String

    fun getLoginButtonText(): String

    fun getRegisterButtonText(): String

    fun getRateLimitError(): String

    fun getServiceError(): String

    fun getNetworkError(): String

}

class VerifyCodeResourceProviderImpl(val resources: Resources) : VerifyCodeResourceProvider {

    override fun formatCodeSentDescription(email: String): String {
        return resources.getString(R.string.verification_code_sent, email)
    }

    override fun getLoginButtonText(): String {
        return resources.getString(R.string.login_button)
    }

    override fun getRegisterButtonText(): String {
        return resources.getString(R.string.register_button)
    }

    override fun getRateLimitError(): String {
        return resources.getString(R.string.error_rate_limit)
    }

    override fun getServiceError(): String {
        return resources.getString(R.string.error_verifying_code)
    }

    override fun getNetworkError(): String {
        return resources.getString(R.string.network_error)
    }

}
