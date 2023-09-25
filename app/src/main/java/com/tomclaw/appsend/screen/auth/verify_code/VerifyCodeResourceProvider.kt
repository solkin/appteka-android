package com.tomclaw.appsend.screen.auth.verify_code

import android.content.res.Resources
import com.tomclaw.appsend.R

interface VerifyCodeResourceProvider {

    fun formatCodeSentDescription(email: String): String

    fun loginButtonText(): String

    fun registerButtonText(): String

    fun rateLimitError(): String

    fun serviceError(): String

    fun networkError(): String

    fun codeFormatInvalid(): String

    fun nameFormatInvalid(): String

}

class VerifyCodeResourceProviderImpl(val resources: Resources) : VerifyCodeResourceProvider {

    override fun formatCodeSentDescription(email: String): String {
        return resources.getString(R.string.verification_code_sent, email)
    }

    override fun loginButtonText(): String {
        return resources.getString(R.string.login_button)
    }

    override fun registerButtonText(): String {
        return resources.getString(R.string.register_button)
    }

    override fun rateLimitError(): String {
        return resources.getString(R.string.error_rate_limit)
    }

    override fun serviceError(): String {
        return resources.getString(R.string.error_verifying_code)
    }

    override fun networkError(): String {
        return resources.getString(R.string.network_error)
    }

    override fun codeFormatInvalid(): String {
        return resources.getString(R.string.code_format_invalid)
    }

    override fun nameFormatInvalid(): String {
        return resources.getString(R.string.name_format_invalid)
    }

}
