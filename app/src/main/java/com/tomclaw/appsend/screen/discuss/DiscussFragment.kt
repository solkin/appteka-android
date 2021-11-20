package com.tomclaw.appsend.screen.discuss

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tomclaw.appsend.R
import com.tomclaw.appsend.main.home.HomeFragment

class DiscussFragment() : HomeFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.discuss_fragment, container, false)
    }
}