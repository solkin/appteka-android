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
