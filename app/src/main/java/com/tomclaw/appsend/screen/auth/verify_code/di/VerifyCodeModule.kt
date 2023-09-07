package com.tomclaw.appsend.screen.auth.verify_code.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.auth.verify_code.VerifyCodeInteractor
import com.tomclaw.appsend.screen.auth.verify_code.VerifyCodeInteractorImpl
import com.tomclaw.appsend.screen.auth.verify_code.VerifyCodePresenter
import com.tomclaw.appsend.screen.auth.verify_code.VerifyCodePresenterImpl
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides
import java.util.Locale

@Module
class VerifyCodeModule(
    private val context: Context,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: VerifyCodeInteractor,
        schedulers: SchedulersFactory
    ): VerifyCodePresenter = VerifyCodePresenterImpl(
        interactor,
        schedulers,
        state
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        api: StoreApi,
        userDataInteractor: UserDataInteractor,
        schedulers: SchedulersFactory
    ): VerifyCodeInteractor = VerifyCodeInteractorImpl(api, userDataInteractor, schedulers)

}
