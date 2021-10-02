package com.tomclaw.appsend.dto

class AppEntity(
    val appId: Int,
    val icon: String?,
    val title: String,
    val verName: String,
    val verCode: Int,
    val size: Long,
    val rating: Float,
    val downloads: Int,
)