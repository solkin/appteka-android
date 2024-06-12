package com.tomclaw.appsend.screen.agreement.di

import android.os.Bundle
import com.tomclaw.appsend.screen.agreement.AgreementPresenter
import com.tomclaw.appsend.screen.agreement.AgreementPresenterImpl
import com.tomclaw.appsend.util.PerActivity
import dagger.Module
import dagger.Provides

@Module
class AgreementModule(
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(): AgreementPresenter = AgreementPresenterImpl(state)

}
