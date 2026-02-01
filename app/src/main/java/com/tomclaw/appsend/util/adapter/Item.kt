package com.tomclaw.appsend.util.adapter

import com.tomclaw.appsend.util.Unobfuscatable

interface Item : Unobfuscatable {
    val id: Long
}
