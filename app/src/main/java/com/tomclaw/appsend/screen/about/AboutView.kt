package com.tomclaw.appsend.screen.about

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.ActionItem
import com.tomclaw.appsend.util.ActionsAdapter
import com.tomclaw.appsend.util.clicks
import io.reactivex.rxjava3.core.Observable

interface AboutView {

    fun setVersion(value: String)

    fun showContributorsDialog(contributors: List<Contributor>)

    fun navigationClicks(): Observable<Unit>

    fun feedbackEmailClicks(): Observable<Unit>

    fun sourceCodeClicks(): Observable<Unit>

    fun telegramGroupClicks(): Observable<Unit>

    fun legalInfoClicks(): Observable<Unit>

    fun contributorsClicks(): Observable<Unit>

    fun contributorClicks(): Observable<Contributor>

}

class AboutViewImpl(view: View) : AboutView {

    private val context: Context = view.context
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val appVersionText: TextView = view.findViewById(R.id.app_version)
    private val feedbackEmailButton: View = view.findViewById(R.id.feedback_email)
    private val sourceCodeButton: View = view.findViewById(R.id.source_code)
    private val telegramGroupButton: View = view.findViewById(R.id.telegram_group)
    private val legalInfoButton: View = view.findViewById(R.id.legal_info)
    private val contributorsButton: View = view.findViewById(R.id.contributors)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val feedbackEmailRelay = PublishRelay.create<Unit>()
    private val sourceCodeRelay = PublishRelay.create<Unit>()
    private val telegramGroupRelay = PublishRelay.create<Unit>()
    private val legalInfoRelay = PublishRelay.create<Unit>()
    private val contributorsRelay = PublishRelay.create<Unit>()
    private val contributorRelay = PublishRelay.create<Contributor>()

    init {
        toolbar.setTitle(R.string.info)
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }

        feedbackEmailButton.clicks(feedbackEmailRelay)
        sourceCodeButton.clicks(sourceCodeRelay)
        telegramGroupButton.clicks(telegramGroupRelay)
        legalInfoButton.clicks(legalInfoRelay)
        contributorsButton.clicks(contributorsRelay)
    }

    override fun setVersion(value: String) {
        appVersionText.text = value
    }

    override fun showContributorsDialog(contributors: List<Contributor>) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val sheetView = View.inflate(context, R.layout.bottom_sheet_actions, null)
        val actionsRecycler: RecyclerView = sheetView.findViewById(R.id.actions_recycler)

        val actions = contributors.mapIndexed { index, contributor ->
            ActionItem(index, contributor.name, R.drawable.ic_github)
        }

        val actionsAdapter = ActionsAdapter(actions) { actionId ->
            bottomSheetDialog.dismiss()
            contributors.getOrNull(actionId)?.let {
                contributorRelay.accept(it)
            }
        }

        actionsRecycler.layoutManager = LinearLayoutManager(context)
        actionsRecycler.adapter = actionsAdapter

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun feedbackEmailClicks(): Observable<Unit> = feedbackEmailRelay

    override fun sourceCodeClicks(): Observable<Unit> = sourceCodeRelay

    override fun telegramGroupClicks(): Observable<Unit> = telegramGroupRelay

    override fun legalInfoClicks(): Observable<Unit> = legalInfoRelay

    override fun contributorsClicks(): Observable<Unit> = contributorsRelay

    override fun contributorClicks(): Observable<Contributor> = contributorRelay

}
