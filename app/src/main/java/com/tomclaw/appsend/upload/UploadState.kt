package com.tomclaw.appsend.upload

data class UploadState(
    val status: UploadStatus,
    val percent: Int = 0,
    val result: UploadResponse? = null,
)

enum class UploadStatus {
    IDLE,
    AWAIT,
    STARTED,
    PROGRESS,
    COMPLETED,
    ERROR,
}

// Reaching one of these is what releases the foreground service
val TERMINAL_UPLOAD_STATUSES = setOf(
    UploadStatus.IDLE,
    UploadStatus.COMPLETED,
    UploadStatus.ERROR,
)
