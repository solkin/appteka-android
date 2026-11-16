package com.tomclaw.appsend.install

sealed interface InstallStatus {

    data object Idle : InstallStatus

    /** Still on us: the APK is being copied into the install session. */
    data class Writing(val percent: Int) : InstallStatus

    /** The system has a question for the user that hasn't reached the screen yet. */
    data object AwaitingConfirmation : InstallStatus

    /** With the system: its question is on screen, or the install itself is running. */
    data object Installing : InstallStatus

}
