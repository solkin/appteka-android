package com.tomclaw.appsend.screen.upload.adapter.prefill_version

import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.screen.upload.adapter.other_versions.VersionItem

class PrefillVersionItem(
    override val id: Long,
    val versions: List<VersionItem>,
    val selectedVersion: VersionItem?
) : Item

