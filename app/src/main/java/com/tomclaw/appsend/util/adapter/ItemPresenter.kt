package com.tomclaw.appsend.util.adapter

interface ItemPresenter<V : ItemView, I : Item> {
    fun bindView(view: V, item: I, position: Int)
}
