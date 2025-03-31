package com.tomclaw.appsend.util

import kotlin.math.min

fun <M> List<M>.trim(max: Int): List<M> {
    return subList(0, min(size, max))
}
