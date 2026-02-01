package com.tomclaw.appsend.screen.details.adapter.user_rate

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Completable
import java.util.concurrent.TimeUnit

class UserRateItemPresenter(
    private val listener: ItemListener,
    private val schedulers: SchedulersFactory,
) : ItemPresenter<UserRateItemView, UserRateItem> {

    override fun bindView(view: UserRateItemView, item: UserRateItem, position: Int) {
        view.setOnRateListener { rating ->
            listener.onRateClick(rating, review = null)
            Completable
                .timer(1, TimeUnit.SECONDS, schedulers.mainThread())
                .subscribe { view.setRating(0f) }
        }
    }

}
