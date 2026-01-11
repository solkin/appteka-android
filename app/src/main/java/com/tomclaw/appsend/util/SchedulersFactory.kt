package com.tomclaw.appsend.util

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers

interface SchedulersFactory {

    fun io(): Scheduler

    fun mainThread(): Scheduler

}

class SchedulersFactoryImpl : SchedulersFactory {

    override fun io(): Scheduler = Schedulers.io()

    override fun mainThread(): Scheduler = AndroidSchedulers.mainThread()

}
