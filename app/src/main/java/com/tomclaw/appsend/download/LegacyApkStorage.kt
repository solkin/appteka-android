package com.tomclaw.appsend.download

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * APK storage implementation for Android 9 (API 28) and below.
 * Stores files in Download/Appteka directory using direct file access.
 * Uses FileProvider for installation URIs.
 */
class LegacyApkStorage(
    private val context: Context
) : ApkStorage {

    private val appsDir: File by lazy {
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            APPTEKA_DIR
        ).apply { mkdirs() }
    }

    override fun openWrite(fileName: String): OutputStream {
        val tmpFile = getTmpFile(fileName)
        tmpFile.parentFile?.mkdirs()
        if (tmpFile.exists()) {
            tmpFile.delete()
        }
        return FileOutputStream(tmpFile)
    }

    override fun commit(fileName: String): Boolean {
        val tmpFile = getTmpFile(fileName)
        val targetFile = getApkFile(fileName)
        return if (tmpFile.exists()) {
            val success = tmpFile.renameTo(targetFile)
            if (success) {
                scanFile(targetFile)
            }
            success
        } else {
            false
        }
    }

    override fun openRead(fileName: String): InputStream? {
        val file = getApkFile(fileName)
        return if (file.exists()) {
            FileInputStream(file)
        } else {
            null
        }
    }

    override fun getInstallUri(fileName: String): Uri? {
        val file = getApkFile(fileName)
        return if (file.exists()) {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } else {
            null
        }
    }

    override fun exists(fileName: String): Boolean {
        return getApkFile(fileName).exists()
    }

    override fun delete(fileName: String): Boolean {
        val file = getApkFile(fileName)
        val deleted = file.delete()
        if (deleted) {
            scanFile(file)
        }
        return deleted
    }

    override fun deleteTmp(fileName: String): Boolean {
        return getTmpFile(fileName).delete()
    }

    override fun getTmpSize(fileName: String): Long {
        val tmpFile = getTmpFile(fileName)
        return if (tmpFile.exists()) tmpFile.length() else 0L
    }

    override fun openAppend(fileName: String): OutputStream {
        val tmpFile = getTmpFile(fileName)
        tmpFile.parentFile?.mkdirs()
        return FileOutputStream(tmpFile, true) // append = true
    }

    override fun listApkFiles(): List<ApkInfo> {
        val files = appsDir.listFiles { file ->
            file.extension.equals(APK_EXTENSION, ignoreCase = true)
        } ?: return emptyList()

        return files.map { file ->
            ApkInfo(
                fileName = file.nameWithoutExtension,
                uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                ),
                size = file.length(),
                lastModified = file.lastModified(),
            )
        }
    }

    override fun clearAll(): Int {
        val files = appsDir.listFiles { file ->
            file.extension.equals(APK_EXTENSION, ignoreCase = true)
        } ?: return 0

        var count = 0
        val deletedFiles = mutableListOf<File>()
        files.forEach { file ->
            deletedFiles.add(file) // Save reference before deletion
            if (file.delete()) {
                count++
            }
        }
        if (deletedFiles.isNotEmpty()) {
            scanFiles(deletedFiles)
        }
        return count
    }

    override fun copyToStorage(input: InputStream, fileName: String): Uri? {
        val targetFile = getApkFile(fileName)
        return try {
            targetFile.parentFile?.mkdirs()
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
                output.flush()
            }
            scanFile(targetFile)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                targetFile
            )
        } catch (ex: Throwable) {
            null
        }
    }

    override fun getFilePath(fileName: String): String? {
        val file = getApkFile(fileName)
        return if (file.exists()) file.absolutePath else null
    }

    override fun isPermissionRequired(): Boolean = true

    private fun getApkFile(fileName: String): File {
        return File(appsDir, "$fileName.$APK_EXTENSION")
    }

    private fun getTmpFile(fileName: String): File {
        return File(appsDir, "$fileName.$APK_EXTENSION.tmp")
    }

    @Suppress("DEPRECATION")
    private fun scanFile(file: File) {
        // Use broadcast for immediate visibility in file managers
        context.sendBroadcast(
            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file))
        )
        // Also use MediaScannerConnection for proper MediaStore update
        MediaScannerConnection.scanFile(
            context,
            arrayOf(file.absolutePath),
            arrayOf(APK_MIME_TYPE),
            null
        )
    }

    private fun scanFiles(files: List<File>) {
        files.forEach { file ->
            scanFile(file)
        }
    }

}

private const val APPTEKA_DIR = "Appteka"
private const val APK_EXTENSION = "apk"
private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
