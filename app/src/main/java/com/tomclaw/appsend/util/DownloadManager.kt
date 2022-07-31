package com.tomclaw.appsend.util

import io.reactivex.rxjava3.core.Observable

interface DownloadManager {

    fun status(packageName: String): Observable<Int>

    fun download(packageName: String)

    fun cancel(packageName: String)

}

class DownloadManagerImpl() : DownloadManager {

    private val packages = HashMap<String, DownloadStatus>()

    override fun status(packageName: String): Observable<Int> {
        return Observable.create {}
    }

    override fun download(packageName: String) {
    }

    override fun cancel(packageName: String) {
    }

}

data class DownloadStatus(
    val status: Int,
    val percent: Int
)
