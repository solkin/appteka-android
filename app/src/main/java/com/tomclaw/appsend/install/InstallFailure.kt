package com.tomclaw.appsend.install

import android.content.pm.PackageInstaller
import androidx.annotation.StringRes
import com.tomclaw.appsend.R

/** Why an install failed, in terms of what the user can do about it. */
enum class InstallFailure(
    @param:StringRes val message: Int,
    val action: Action,
) {

    SIGNATURE(R.string.install_failed_signature, Action.REMOVE),
    DOWNGRADE(R.string.install_failed_downgrade, Action.REMOVE),
    CONFLICT(R.string.install_failed_conflict, Action.REMOVE),
    ABI(R.string.install_failed_abi, Action.NONE),
    ANDROID_TOO_OLD(R.string.install_failed_android_too_old, Action.NONE),
    APP_TOO_OLD(R.string.install_failed_app_too_old, Action.NONE),
    INCOMPATIBLE(R.string.install_failed_incompatible, Action.NONE),
    INVALID(R.string.install_failed_invalid, Action.NONE),
    NO_SPACE(R.string.install_failed_storage, Action.FREE_SPACE),
    BLOCKED(R.string.install_failed_blocked, Action.SYSTEM_INSTALLER),
    UNKNOWN(R.string.install_failed, Action.SYSTEM_INSTALLER);

    enum class Action { NONE, REMOVE, FREE_SPACE, SYSTEM_INSTALLER }

    companion object {

        /**
         * The public status lumps together causes that need opposite advice, such as a
         * downgrade and a broken file. The status message starts with the exact code, but
         * nothing promises that format, so the status alone must still give a true answer.
         *
         * Returns null when there is nothing to report: the user said no themselves.
         */
        fun from(status: Int, statusMessage: String?): InstallFailure? {
            val code = statusMessage?.substringBefore(':')?.trim()
            return BY_CODE[code] ?: when (status) {
                PackageInstaller.STATUS_FAILURE_ABORTED -> null
                PackageInstaller.STATUS_FAILURE_BLOCKED -> BLOCKED
                PackageInstaller.STATUS_FAILURE_CONFLICT -> CONFLICT
                PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> INCOMPATIBLE
                PackageInstaller.STATUS_FAILURE_INVALID -> INVALID
                PackageInstaller.STATUS_FAILURE_STORAGE -> NO_SPACE
                else -> UNKNOWN
            }
        }

        private val BY_CODE = mapOf(
            "INSTALL_FAILED_UPDATE_INCOMPATIBLE" to SIGNATURE,
            "INSTALL_FAILED_VERSION_DOWNGRADE" to DOWNGRADE,
            "INSTALL_FAILED_PERMISSION_MODEL_DOWNGRADE" to DOWNGRADE,
            "INSTALL_FAILED_NO_MATCHING_ABIS" to ABI,
            "INSTALL_FAILED_CPU_ABI_INCOMPATIBLE" to ABI,
            "INSTALL_FAILED_OLDER_SDK" to ANDROID_TOO_OLD,
            "INSTALL_FAILED_NEWER_SDK" to APP_TOO_OLD,
            "INSTALL_FAILED_DEPRECATED_SDK_VERSION" to APP_TOO_OLD,
            "INSTALL_FAILED_USER_RESTRICTED" to BLOCKED,
            // Reported as aborted, which would otherwise pass for the user's own refusal
            "INSTALL_FAILED_VERIFICATION_FAILURE" to BLOCKED,
            "INSTALL_FAILED_VERIFICATION_TIMEOUT" to BLOCKED,
        )

    }

}
