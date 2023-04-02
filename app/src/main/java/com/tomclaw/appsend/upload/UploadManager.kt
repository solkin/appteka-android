package com.tomclaw.appsend.upload

import io.reactivex.rxjava3.core.Observable

interface UploadManager {

    fun status(id: String): Observable<Int>

    fun upload(id: String, meta: MetaInfo)

}

class UploadManagerImpl : UploadManager {

    override fun status(id: String): Observable<Int> {
        TODO("Not yet implemented")
    }

    override fun upload(id: String, meta: MetaInfo) {
        TODO("Not yet implemented")
    }

}