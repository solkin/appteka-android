package com.tomclaw.appsend.core

// Where a build comes from, as its uploader declares it. Raw codes, not an
// enum: a code a newer server invents must not break parsing — it just
// shows nothing, same as a build that never declared one.
object Origin {

    // The uploader's own app. Until an admin binds the package to them,
    // this is only a claim and reads as an original build.
    const val OWN = "own"
    const val MIRROR = "mirror"
    const val MOD = "mod"

}
