package com.tomclaw.appsend.util

/**
 * Marker annotation for classes that are serialized/deserialized with Gson.
 * Classes annotated with @GsonModel will be kept from ProGuard/R8 obfuscation
 * to ensure proper JSON serialization.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class GsonModel
