package com.tomclaw.appsend.screen.upload

import android.net.Uri
import android.os.Build
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.categories.CategoryItem
import com.tomclaw.appsend.screen.upload.adapter.category.SelectCategoryItem
import com.tomclaw.appsend.screen.upload.adapter.description.DescriptionItem
import com.tomclaw.appsend.screen.upload.adapter.exclusive.ExclusiveItem
import com.tomclaw.appsend.screen.upload.adapter.notice.NoticeItem
import com.tomclaw.appsend.screen.upload.adapter.notice.NoticeType
import com.tomclaw.appsend.screen.upload.adapter.open_source.OpenSourceItem
import com.tomclaw.appsend.screen.upload.adapter.other_versions.OtherVersionsItem
import com.tomclaw.appsend.screen.upload.adapter.other_versions.VersionItem
import com.tomclaw.appsend.screen.upload.adapter.screen_append.ScreenAppendItem
import com.tomclaw.appsend.screen.upload.adapter.screen_image.ScreenImageItem
import com.tomclaw.appsend.screen.upload.adapter.screenshots.ScreenshotsItem
import com.tomclaw.appsend.screen.upload.adapter.select_app.SelectAppItem
import com.tomclaw.appsend.screen.upload.adapter.selected_app.SelectedAppItem
import com.tomclaw.appsend.screen.upload.adapter.submit.SubmitItem
import com.tomclaw.appsend.screen.upload.adapter.whats_new.WhatsNewItem
import com.tomclaw.appsend.screen.upload.api.CheckExistResponse
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadPackage
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
        sourceUrl: String,
        highlightErrors: Boolean,
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
        highlightErrors: Boolean,
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
        items += ScreenshotsItem(
            id = id++,
            items = listOf(
                ScreenAppendItem(id++),
                ScreenImageItem(
                    id++,
                    Uri.parse("https://f-droid.org/repo/com.github.cvzi.screenshottile/en-US/phoneScreenshots/1_en-US.png"),
                    1080,
                    2220
                ),
                ScreenImageItem(
                    id++,
                    Uri.parse("https://cdn.digitbin.com/wp-content/uploads/Display_options.jpg"),
                    1080,
                    2412
                ),
                ScreenImageItem(
                    id++,
                    Uri.parse("https://i.stack.imgur.com/fNbz0.png"),
                    252,
                    448
                ),
                ScreenImageItem(
                    id++,
                    Uri.parse("https://cdn.afterdawn.fi/storage/pictures/1920/guide-force-landscape-android-landscape.jpg"),
                    1920,
                    1080
                ),
                ScreenImageItem(
                    id++,
                    Uri.parse("https://images.wondershare.com/images/mobile/mobilego/android-screenshot2.jpg"),
                    281,
                    500
                ),
                ScreenImageItem(
                    id++,
                    Uri.parse("https://www.guidingtech.com/wp-content/uploads/Take-Screenshot-With-Google-Assistant.jpg"),
                    782,
                    1602
                )
            )
        )

        if (isUploadAvailable) {
            items += SelectCategoryItem(
                id++,
                category = category,
                errorRequiredField = highlightErrors && category == null
            )
            items += WhatsNewItem(id++, text = whatsNew)
            items += DescriptionItem(
                id++,
                text = description,
                errorRequiredField = highlightErrors && description.isBlank()
            )
            items += ExclusiveItem(id++, value = exclusive)
            items += OpenSourceItem(id++, value = openSource, url = sourceUrl)
            items += SubmitItem(id++, editMode = isEditMode, enabled = (isEditMode || apk != null))
        }

        return items
    }

}
