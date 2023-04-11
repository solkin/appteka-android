package com.tomclaw.appsend.categories

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Single

interface CategoriesInteractor {

    fun getCategories(): Single<List<Category>>

}

class CategoriesInteractorImpl(
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : CategoriesInteractor {

    private var categories: List<Category>? = null

    override fun getCategories(): Single<List<Category>> {
        return Single
            .create { emitter ->
                categories?.let {
                    emitter.onSuccess(it)
                } ?: emitter.onError(Exception("No cached categories"))
            }
            .onErrorResumeWith(
                api.getCategories()
                    .map {
                        val categoriesSorted = it.result.categories
                        categories = categoriesSorted
                        categoriesSorted
                    }
                    .subscribeOn(schedulers.io())
            )
    }

}
