package com.tomclaw.appsend.screen.details.adapter.controls

import android.view.View
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.disable
import com.tomclaw.appsend.util.enable
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show

interface ControlsItemView : ItemView {

    fun showInstallButton()

    fun disableInstallButton()

    fun showUpdateButton()

    fun disableUpdateButton()

    fun showLaunchButton()

    fun disableLaunchButton()

    fun showRemoveButton()

    fun showCancelButton()

    fun hideButtons()

    fun enableButtons()

    fun setOnInstallClickListener(listener: (() -> Unit)?)

    fun setOnUpdateClickListener(listener: (() -> Unit)?)

    fun setOnLaunchClickListener(listener: (() -> Unit)?)

    fun setOnRemoveClickListener(listener: (() -> Unit)?)

    fun setOnCancelClickListener(listener: (() -> Unit)?)

}

class ControlsItemViewHolder(view: View) : BaseItemViewHolder(view), ControlsItemView {

    private val installButton: View = view.findViewById(R.id.install_button)
    private val updateButton: View = view.findViewById(R.id.update_button)
    private val launchButton: View = view.findViewById(R.id.launch_button)
    private val removeButton: View = view.findViewById(R.id.remove_button)
    private val cancelButton: View = view.findViewById(R.id.cancel_button)

    private var installClickListener: (() -> Unit)? = null
    private var updateClickListener: (() -> Unit)? = null
    private var launchClickListener: (() -> Unit)? = null
    private var removeClickListener: (() -> Unit)? = null
    private var cancelClickListener: (() -> Unit)? = null

    init {
        installButton.setOnClickListener { installClickListener?.invoke() }
        updateButton.setOnClickListener { updateClickListener?.invoke() }
        launchButton.setOnClickListener { launchClickListener?.invoke() }
        removeButton.setOnClickListener { removeClickListener?.invoke() }
        cancelButton.setOnClickListener { cancelClickListener?.invoke() }
    }

    override fun showInstallButton() {
        installButton.show()
    }

    override fun disableInstallButton() {
        installButton.disable()
    }

    override fun showUpdateButton() {
        updateButton.show()
    }

    override fun disableUpdateButton() {
        updateButton.disable()
    }

    override fun showLaunchButton() {
        launchButton.show()
    }

    override fun disableLaunchButton() {
        launchButton.disable()
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
        launchButton.hide()
        removeButton.hide()
        cancelButton.hide()
    }

    override fun enableButtons() {
        installButton.enable()
        updateButton.enable()
        launchButton.enable()
    }

    override fun setOnInstallClickListener(listener: (() -> Unit)?) {
        this.installClickListener = listener
    }

    override fun setOnUpdateClickListener(listener: (() -> Unit)?) {
        this.updateClickListener = listener
    }

    override fun setOnLaunchClickListener(listener: (() -> Unit)?) {
        this.launchClickListener = listener
    }

    override fun setOnRemoveClickListener(listener: (() -> Unit)?) {
        this.removeClickListener = listener
    }

    override fun setOnCancelClickListener(listener: (() -> Unit)?) {
        this.cancelClickListener = listener
    }

    override fun onUnbind() {
        this.installClickListener = null
        this.updateClickListener = null
        this.launchClickListener = null
        this.removeClickListener = null
        this.cancelClickListener = null
    }

}
