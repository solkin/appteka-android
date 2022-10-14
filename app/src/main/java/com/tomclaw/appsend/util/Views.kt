package com.tomclaw.appsend.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.TextView
import com.jakewharton.rxrelay3.Relay

fun TextView.bind(value: String?) {
    if (TextUtils.isEmpty(value)) {
        visibility = View.GONE
        text = ""
    } else {
        visibility = View.VISIBLE
        text = value
    }
}

fun View.clicks(relay: Relay<Unit>) {
    setOnClickListener { relay.accept(Unit) }
}

fun EditText.changes(handler: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable) {}

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            handler.invoke(s.toString())
        }
    })
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.enable() {
    isEnabled = true
}

fun View.disable() {
    isEnabled = false
}

fun View.showWithAlphaAnimation(
    duration: Long = ANIMATION_DURATION_SHORT,
    animateFully: Boolean = true,
    endCallback: (() -> Unit)? = null
): ViewPropertyAnimator {
    if (animateFully) {
        alpha = 0.0f
    }
    show()
    return animate()
        .setDuration(duration)
        .alpha(1.0f)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                alpha = 1.0f
                show()
                endCallback?.invoke()
            }
        })
}

fun View.hideWithAlphaAnimation(
    duration: Long = ANIMATION_DURATION_SHORT,
    animateFully: Boolean = true,
    endCallback: (() -> Unit)? = null
): ViewPropertyAnimator {
    if (animateFully) {
        alpha = 1.0f
    }
    return animate()
        .setDuration(duration)
        .alpha(0.0f)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                hide()
                alpha = 1.0f
                endCallback?.invoke()
            }
        })
}

fun View.scaleWithAnimation(
    factor: Float,
    duration: Long = ANIMATION_DURATION_LONG,
    endCallback: (() -> Unit)? = null,
): ViewPropertyAnimator {
    return animate()
        .setDuration(duration)
        .scaleX(factor)
        .scaleY(factor)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                endCallback?.invoke()
            }
        })
}

const val ANIMATION_DURATION_SHORT: Long = 250
const val ANIMATION_DURATION_LONG: Long = 350
