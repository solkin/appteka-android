package com.tomclaw.appsend.screen.auth.verify_code.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.auth.verify_code.VerifyCodeInteractor
import com.tomclaw.appsend.screen.auth.verify_code.VerifyCodeInteractorImpl
import com.tomclaw.appsend.screen.auth.verify_code.VerifyCodePresenter
import com.tomclaw.appsend.screen.auth.verify_code.VerifyCodePresenterImpl
import com.tomclaw.appsend.screen.auth.verify_code.VerifyCodeResourceProvider
import com.tomclaw.appsend.screen.auth.verify_code.VerifyCodeResourceProviderImpl
import com.tomclaw.appsend.user.SessionStorage
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides

@Module
class VerifyCodeModule(
    private val context: Context,
    private val email: String,
    private val requestId: String,
    private val registered: Boolean,
    private val codeRegex: String,
    private val nameRegex: String,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        resourceProvider: VerifyCodeResourceProvider,
        interactor: VerifyCodeInteractor,
        schedulers: SchedulersFactory
    ): VerifyCodePresenter = VerifyCodePresenterImpl(
        email,
        requestId,
        registered,
        codeRegex,
        nameRegex,
        resourceProvider,
        interactor,
        schedulers,
        state
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        api: StoreApi,
        sessionStorage: SessionStorage,
        schedulers: SchedulersFactory
    ): VerifyCodeInteractor = VerifyCodeInteractorImpl(api, sessionStorage, schedulers)

    @Provides
    @PerActivity
    internal fun provideVerifyCodeResourceProvider(): VerifyCodeResourceProvider =
        VerifyCodeResourceProviderImpl(resources = context.resources)

}
