package com.tomclaw.appsend.core

enum class RatingScope {
    EDITION,
    BUILD;

    companion object {
        fun parse(value: String?): RatingScope {
            return entries.firstOrNull { it.name == value } ?: EDITION
        }
    }
}
