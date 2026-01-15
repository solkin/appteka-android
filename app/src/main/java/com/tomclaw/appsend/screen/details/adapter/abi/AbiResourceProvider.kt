package com.tomclaw.appsend.screen.details.adapter.abi

import android.content.res.Resources
import android.os.Build
import com.tomclaw.appsend.R

interface AbiResourceProvider {

    fun formatAbiName(abi: String): String

    fun getCompatibilityText(isCompatible: Boolean): String

    fun getDeviceSupportedAbis(): List<String>

    fun checkCompatibility(abiList: List<String>): Boolean

}

class AbiResourceProviderImpl(
    private val resources: Resources
) : AbiResourceProvider {

    override fun formatAbiName(abi: String): String {
        return when (abi) {
            ABI_UNIVERSAL -> resources.getString(R.string.abi_universal)
            ABI_ARM64_V8A -> resources.getString(R.string.abi_arm64)
            ABI_ARMEABI_V7A -> resources.getString(R.string.abi_arm32)
            ABI_ARMEABI -> resources.getString(R.string.abi_arm)
            ABI_X86_64 -> resources.getString(R.string.abi_x86_64)
            ABI_X86 -> resources.getString(R.string.abi_x86)
            ABI_MIPS64 -> resources.getString(R.string.abi_mips64)
            ABI_MIPS -> resources.getString(R.string.abi_mips)
            else -> abi
        }
    }

    override fun getCompatibilityText(isCompatible: Boolean): String {
        return if (isCompatible) {
            resources.getString(R.string.abi_compatible)
        } else {
            resources.getString(R.string.abi_incompatible)
        }
    }

    override fun getDeviceSupportedAbis(): List<String> {
        return Build.SUPPORTED_ABIS.toList()
    }

    override fun checkCompatibility(abiList: List<String>): Boolean {
        if (abiList.isEmpty()) {
            // Unknown ABI (legacy entries) - treat as compatible
            return true
        }
        if (abiList.contains(ABI_UNIVERSAL)) {
            // Universal - compatible with all
            return true
        }
        val deviceAbis = getDeviceSupportedAbis()
        return abiList.any { it in deviceAbis }
    }

}

const val ABI_UNIVERSAL = "universal"
const val ABI_ARM64_V8A = "arm64-v8a"
const val ABI_ARMEABI_V7A = "armeabi-v7a"
const val ABI_ARMEABI = "armeabi"
const val ABI_X86_64 = "x86_64"
const val ABI_X86 = "x86"
const val ABI_MIPS64 = "mips64"
const val ABI_MIPS = "mips"
