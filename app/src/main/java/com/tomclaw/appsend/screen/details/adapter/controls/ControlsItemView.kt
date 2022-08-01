package com.tomclaw.appsend.screen.details.adapter.controls

import android.view.View
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show

interface ControlsItemView : ItemView {

    fun showInstallButton()

    fun showUpdateButton()

    fun showOpenButton()

    fun showRemoveButton()

    fun showCancelButton()

    fun hideButtons()

    fun setOnInstallClickListener(listener: (() -> Unit)?)

    fun setOnOpenClickListener(listener: (() -> Unit)?)

    fun setOnRemoveClickListener(listener: (() -> Unit)?)

}

class ControlsItemViewHolder(view: View) : BaseViewHolder(view), ControlsItemView {

    private val installButton: View = view.findViewById(R.id.install_button)
    private val updateButton: View = view.findViewById(R.id.update_button)
    private val openButton: View = view.findViewById(R.id.open_button)
    private val removeButton: View = view.findViewById(R.id.remove_button)
    private val cancelButton: View = view.findViewById(R.id.cancel_button)

    private var installClickListener: (() -> Unit)? = null
    private var updateClickListener: (() -> Unit)? = null
    private var openClickListener: (() -> Unit)? = null
    private var removeClickListener: (() -> Unit)? = null
    private var cancelClickListener: (() -> Unit)? = null

    init {
        installButton.setOnClickListener { installClickListener?.invoke() }
        updateButton.setOnClickListener { updateClickListener?.invoke() }
        openButton.setOnClickListener { openClickListener?.invoke() }
        removeButton.setOnClickListener { removeClickListener?.invoke() }
        cancelButton.setOnClickListener { cancelClickListener?.invoke() }
    }

    override fun showInstallButton() {
        installButton.show()
    }

    override fun showUpdateButton() {
        updateButton.show()
    }

    override fun showOpenButton() {
        openButton.show()
    }

    override fun showRemoveButton() {
        removeButton.show()
    }

    override fun showCancelButton() {
        cancelButton.show()
    }

    override fun hideButtons() {
        installButton.hide()
        updateButton.hide()
        openButton.hide()
        removeButton.hide()
        cancelButton.hide()
    }

    override fun setOnInstallClickListener(listener: (() -> Unit)?) {
        this.installClickListener = listener
    }

    override fun setOnOpenClickListener(listener: (() -> Unit)?) {
        this.openClickListener = listener
    }

    override fun setOnRemoveClickListener(listener: (() -> Unit)?) {
        this.removeClickListener = listener
    }

    override fun onUnbind() {
        this.installClickListener = null
        this.updateClickListener = null
        this.openClickListener = null
        this.removeClickListener = null
        this.cancelClickListener = null
    }

}
