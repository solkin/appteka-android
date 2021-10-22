package com.tomclaw.appsend.util

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
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
