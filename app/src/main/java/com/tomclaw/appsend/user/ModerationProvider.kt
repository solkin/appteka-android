package com.tomclaw.appsend.user

import com.tomclaw.appsend.screen.home.api.ModerationData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

interface ModerationProvider {

    fun getModerationData(): ModerationData?

    fun setModerationData(data: ModerationData?)

    fun updateModerationCount(count: Int)

    fun observeModerationData(): Observable<ModerationData>

}

class ModerationProviderImpl : ModerationProvider {

    private var moderationData: ModerationData? = null
    private val moderationSubject = BehaviorSubject.create<ModerationData>()

    override fun getModerationData(): ModerationData? = moderationData

    override fun setModerationData(data: ModerationData?) {
        moderationData = data
        data?.let { moderationSubject.onNext(it) }
    }

    override fun updateModerationCount(count: Int) {
        moderationData = moderationData?.copy(count = count)
        moderationData?.let { moderationSubject.onNext(it) }
    }

    override fun observeModerationData(): Observable<ModerationData> = moderationSubject

}

