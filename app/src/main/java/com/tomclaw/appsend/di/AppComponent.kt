package com.tomclaw.appsend.di

import com.tomclaw.appsend.download.di.DownloadServiceComponent
import com.tomclaw.appsend.download.di.DownloadServiceModule
import com.tomclaw.appsend.screen.chat.di.ChatComponent
import com.tomclaw.appsend.screen.chat.di.ChatModule
import com.tomclaw.appsend.screen.details.di.DetailsComponent
import com.tomclaw.appsend.screen.details.di.DetailsModule
import com.tomclaw.appsend.screen.moderation.di.ModerationComponent
import com.tomclaw.appsend.screen.moderation.di.ModerationModule
import com.tomclaw.appsend.screen.rate.di.RateComponent
import com.tomclaw.appsend.screen.rate.di.RateModule
import com.tomclaw.appsend.screen.store.di.StoreComponent
import com.tomclaw.appsend.screen.store.di.StoreModule
import com.tomclaw.appsend.screen.topics.di.TopicsComponent
import com.tomclaw.appsend.screen.topics.di.TopicsModule
import com.tomclaw.appsend.screen.upload.di.UploadComponent
import com.tomclaw.appsend.screen.upload.di.UploadModule
import com.tomclaw.appsend.upload.di.UploadServiceComponent
import com.tomclaw.appsend.upload.di.UploadServiceModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun moderationComponent(module: ModerationModule): ModerationComponent

    fun topicsComponent(module: TopicsModule): TopicsComponent

    fun chatComponent(module: ChatModule): ChatComponent

    fun storeComponent(module: StoreModule): StoreComponent

    fun detailsComponent(module: DetailsModule): DetailsComponent

    fun rateComponent(module: RateModule): RateComponent

    fun uploadComponent(module: UploadModule): UploadComponent

    fun downloadServiceComponent(module: DownloadServiceModule): DownloadServiceComponent

    fun uploadServiceComponent(module: UploadServiceModule): UploadServiceComponent

}
