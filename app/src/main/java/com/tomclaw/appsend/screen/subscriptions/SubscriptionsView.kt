package com.tomclaw.appsend.screen.subscriptions

import android.annotation.SuppressLint
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.capitalize
import io.reactivex.rxjava3.core.Observable
import java.util.Locale

interface SubscriptionsView {

    fun setSelectedPage(index: Int)

    fun navigationClicks(): Observable<Unit>

    @SuppressLint("NotifyDataSetChanged")
    fun contentUpdated()

}

class SubscriptionsViewImpl(
    view: View,
    private val adapter: SubscriptionsAdapter
) : SubscriptionsView {

    private val context = view.context
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val tabs: TabLayout = view.findViewById(R.id.tabs)
    private val pager: ViewPager2 = view.findViewById(R.id.pager)

    private val navigationRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }

        pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        pager.adapter = adapter

        TabLayoutMediator(tabs, pager) { tab, position ->
            tab.text = context.getString(adapter.getItemTitle(position))
                .capitalize(Locale.getDefault())
        }.attach()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun setSelectedPage(index: Int) {
        pager.setCurrentItem(index, false)
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

}