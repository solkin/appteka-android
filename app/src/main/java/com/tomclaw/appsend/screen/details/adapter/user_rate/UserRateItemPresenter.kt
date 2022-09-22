package com.tomclaw.appsend.screen.details.adapter.user_rate

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.core.MainExecutor
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import java.util.concurrent.TimeUnit

class UserRateItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<UserRateItemView, UserRateItem> {

    override fun bindView(view: UserRateItemView, item: UserRateItem, position: Int) {
        view.setOnRateListener { rating ->
            listener.onRateClick(rating, review = null)
            MainExecutor.executeLater({
                view.setRating(0f)
            }, TimeUnit.SECONDS.toMillis(1))
        }
    }

}
