package com.tomclaw.appsend.screen.upload

import android.os.Build
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.categories.CategoryItem
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.screen.upload.adapter.category.SelectCategoryItem
import com.tomclaw.appsend.screen.upload.adapter.description.DescriptionItem
import com.tomclaw.appsend.screen.upload.adapter.exclusive.ExclusiveItem
import com.tomclaw.appsend.screen.upload.adapter.notice.NoticeItem
import com.tomclaw.appsend.screen.upload.adapter.notice.NoticeType
import com.tomclaw.appsend.screen.upload.adapter.open_source.OpenSourceItem
import com.tomclaw.appsend.screen.upload.adapter.other_versions.OtherVersionsItem
import com.tomclaw.appsend.screen.upload.adapter.other_versions.VersionItem
import com.tomclaw.appsend.screen.upload.adapter.select_app.SelectAppItem
import com.tomclaw.appsend.screen.upload.adapter.selected_app.SelectedAppItem
import com.tomclaw.appsend.screen.upload.adapter.submit.SubmitItem
import com.tomclaw.appsend.screen.upload.adapter.whats_new.WhatsNewItem
import com.tomclaw.appsend.screen.upload.api.CheckExistResponse
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.util.versionCodeCompat

interface UploadConverter {

    fun convert(
        pkg: UploadPackage?,
        apk: UploadApk?,
        checkExist: CheckExistResponse?,
        category: CategoryItem?,
        whatsNew: String,
        description: String,
        exclusive: Boolean,
        openSource: Boolean,
        sourceUrl: String
    ): List<Item>

}

class UploadConverterImpl(
    private val resourceProvider: UploadResourceProvider
) : UploadConverter {

    override fun convert(
        pkg: UploadPackage?,
        apk: UploadApk?,
        checkExist: CheckExistResponse?,
        category: CategoryItem?,
        whatsNew: String,
        description: String,
        exclusive: Boolean,
        openSource: Boolean,
        sourceUrl: String,
    ): List<Item> {
        var id: Long = 1
        val items = ArrayList<Item>()
        val isEditMode = checkExist?.file?.appId != null

        if (apk != null) {
            items += SelectedAppItem(id++, apk)
        } else if (pkg == null) {
            items += SelectAppItem(id++)
        }

        var isUploadAvailable = true

        checkExist?.let {
            val clickable = checkExist.file != null
            if (checkExist.info?.isEmpty() == false) {
                items += NoticeItem(id++, NoticeType.INFO, checkExist.info, clickable)
            }
            if (checkExist.warning?.isEmpty() == false) {
                items += NoticeItem(id++, NoticeType.WARNING, checkExist.warning, clickable)
            }
            if (checkExist.error?.isEmpty() == false) {
                items += NoticeItem(id++, NoticeType.ERROR, checkExist.error, clickable)
                isUploadAvailable = false
            }

            checkExist.versions
                ?.takeIf { it.isNotEmpty() }
                ?.run {
                    val versions = this
                        .sortedBy { it.verCode }
                        .reversed()
                        .map { version ->
                            VersionItem(
                                versionId = version.appId.hashCode(),
                                appId = version.appId,
                                title = resourceProvider.formatVersion(version),
                                compatible = version.sdkVersion <= Build.VERSION.SDK_INT,
                                newer = apk?.packageInfo?.versionCodeCompat()
                                    ?.let { version.verCode > it } ?: false,
                            )
                        }
                    items += OtherVersionsItem(id++, versions)
                }
        }

        if (isUploadAvailable) {
            items += SelectCategoryItem(id++, category = category)
            items += WhatsNewItem(id++, text = whatsNew)
            items += DescriptionItem(id++, text = description)
            items += ExclusiveItem(id++, value = exclusive)
            items += OpenSourceItem(id++, value = openSource, url = sourceUrl)
            items += SubmitItem(id++, isEditMode)
        }

        return items
    }

}
