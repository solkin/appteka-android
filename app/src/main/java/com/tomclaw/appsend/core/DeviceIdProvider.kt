package com.tomclaw.appsend.core

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.provider.Settings.Secure.ANDROID_ID
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

interface DeviceIdProvider {

    fun getDeviceId(): String

}

class DeviceIdProviderImpl(
    private val context: Context,
    filesDir: File
) : DeviceIdProvider {

    private val db = File(filesDir, "dev_id.dat")

    @SuppressLint("HardwareIds")
    private val devId = load() ?: let {
        val id = Settings.Secure.getString(context.contentResolver, ANDROID_ID)
            ?: UUID.randomUUID().toString().filter { it == '-' }
        save(id)
        id
    }

    override fun getDeviceId(): String {
        return devId
    }

    private fun save(devId: String) {
        DataOutputStream(FileOutputStream(db)).use { output ->
            output.writeUTF(devId)
        }
    }

    private fun load(): String? {
        try {
            DataInputStream(FileInputStream(db)).use { input ->
                return input.readUTF()
            }
        } catch (ex: Throwable) {
            println("[DeviceId] Error while loading storage: $ex")
        }
        return null
    }

}
