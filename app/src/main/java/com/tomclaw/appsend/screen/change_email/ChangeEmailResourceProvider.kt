package com.tomclaw.appsend.screen.change_email

import android.content.res.Resources
import com.tomclaw.appsend.R

interface ChangeEmailResourceProvider {

    fun getCodeSentMessage(email: String): String

    fun getInvalidEmailError(): String

    fun getInvalidCodeError(): String

    fun getEmailAlreadyTakenError(): String

    fun getDomainBlockedError(): String

    fun getRateLimitError(): String

    fun getServiceError(): String

    fun getNetworkError(): String

    fun getEmailChangedSuccess(): String

}

class ChangeEmailResourceProviderImpl(
    private val resources: Resources
) : ChangeEmailResourceProvider {

    override fun getCodeSentMessage(email: String): String {
        return resources.getString(R.string.code_sent_to_email, email)
    }

    override fun getInvalidEmailError(): String {
        return resources.getString(R.string.invalid_email_format)
    }

    override fun getInvalidCodeError(): String {
        return resources.getString(R.string.invalid_code_format)
    }

    override fun getEmailAlreadyTakenError(): String {
        return resources.getString(R.string.email_already_taken)
    }

    override fun getDomainBlockedError(): String {
        return resources.getString(R.string.error_domain_blocked)
    }

    override fun getRateLimitError(): String {
        return resources.getString(R.string.error_rate_limit)
    }

    override fun getServiceError(): String {
        return resources.getString(R.string.error_changing_email)
    }

    override fun getNetworkError(): String {
        return resources.getString(R.string.network_error)
    }

    override fun getEmailChangedSuccess(): String {
        return resources.getString(R.string.email_changed_success)
    }

}
