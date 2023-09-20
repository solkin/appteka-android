package com.tomclaw.appsend.screen.auth.verify_code

import android.content.res.Resources
import com.tomclaw.appsend.R

interface VerifyCodeResourceProvider {

    fun formatCodeSentDescription(email: String): String

}

class VerifyCodeResourceProviderImpl(val resources: Resources) : VerifyCodeResourceProvider {

    override fun formatCodeSentDescription(email: String): String {
        return resources.getString(R.string.verification_code_sent, email)
    }

}
