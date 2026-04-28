package com.tomclaw.appsend.screen.moderation

import android.content.res.Resources
import com.tomclaw.appsend.R

interface ModerationResourceProvider {

    fun votingHintFinal(): String

    fun votingHintAdvisory(): String

}

class ModerationResourceProviderImpl(val resources: Resources) : ModerationResourceProvider {

    override fun votingHintFinal(): String =
        resources.getString(R.string.permission_moderation_final_vote)

    override fun votingHintAdvisory(): String =
        resources.getString(R.string.permission_moderation_advisory_vote)

}
