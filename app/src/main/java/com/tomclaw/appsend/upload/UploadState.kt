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

const val IDLE: Int = -30
const val AWAIT: Int = -10
const val STARTED: Int = -20
const val PROGRESS: Int = 0
const val COMPLETED: Int = 101
const val ERROR: Int = -40
