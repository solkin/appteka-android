package com.tomclaw.appsend.download

import android.net.Uri

/** Where a download stands. The last state stays until the next download or a cancel. */
sealed interface DownloadState {

    data object Idle : DownloadState

    /** Waiting for its turn: downloads run one at a time. */
    data object Queued : DownloadState

    /** On its way, with [percent] once the size is known. */
    data class Running(val percent: Int?) : DownloadState

    data class Completed(val uri: Uri) : DownloadState

    data object Failed : DownloadState

}
