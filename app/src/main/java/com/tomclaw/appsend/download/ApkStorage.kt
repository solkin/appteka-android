package com.tomclaw.appsend.download

import android.net.Uri
import java.io.InputStream
import java.io.OutputStream

/**
 * Information about stored APK file.
 */
data class ApkInfo(
    val fileName: String,
    val uri: Uri,
    val size: Long,
    val lastModified: Long,
)

/**
 * Abstraction for APK file storage.
 * Different implementations handle different Android versions:
 * - LegacyApkStorage for API < 29 (direct file access to Download/Appteka)
 * - MediaStoreApkStorage for API >= 29 (MediaStore API)
 */
interface ApkStorage {

    /**
     * Open output stream for writing APK file.
     * Creates a temporary file that should be committed after successful download.
     * @param fileName target file name (without extension)
     * @return output stream for writing
     */
    fun openWrite(fileName: String): OutputStream

    /**
     * Commit temporary file after successful download.
     * Renames .tmp file to .apk
     * @param fileName target file name (without extension)
     * @return true if commit was successful
     */
    fun commit(fileName: String): Boolean

    /**
     * Open input stream for reading APK file.
     * @param fileName target file name (without extension)
     * @return input stream or null if file doesn't exist
     */
    fun openRead(fileName: String): InputStream?

    /**
     * Get URI for installing APK.
     * For legacy storage - FileProvider URI.
     * For MediaStore - content:// URI.
     * @param fileName target file name (without extension)
     * @return URI for installation or null if file doesn't exist
     */
    fun getInstallUri(fileName: String): Uri?

    /**
     * Check if APK file exists.
     * @param fileName target file name (without extension)
     * @return true if file exists
     */
    fun exists(fileName: String): Boolean

    /**
     * Delete APK file.
     * @param fileName target file name (without extension)
     * @return true if file was deleted
     */
    fun delete(fileName: String): Boolean

    /**
     * Delete temporary file (used when download is cancelled or failed).
     * @param fileName target file name (without extension)
     * @return true if file was deleted
     */
    fun deleteTmp(fileName: String): Boolean

    /**
     * Get size of temporary file (for resume download).
     * @param fileName target file name (without extension)
     * @return size in bytes or 0 if file doesn't exist
     */
    fun getTmpSize(fileName: String): Long

    /**
     * Open output stream for appending to existing temporary file (for resume download).
     * If temporary file doesn't exist, creates a new one.
     * @param fileName target file name (without extension)
     * @return output stream in append mode
     */
    fun openAppend(fileName: String): OutputStream

    /**
     * List all APK files in storage.
     * @return list of APK file information
     */
    fun listApkFiles(): List<ApkInfo>

    /**
     * Clear all APK files from storage.
     * @return number of deleted files
     */
    fun clearAll(): Int

    /**
     * Copy content from input stream to storage.
     * @param input source input stream
     * @param fileName target file name (without extension)
     * @return URI of created file or null on error
     */
    fun copyToStorage(input: InputStream, fileName: String): Uri?

    /**
     * Get file path for APK (for PackageManager operations).
     * On Android 10+ this returns a cached copy path.
     * @param fileName target file name (without extension)
     * @return file path or null if not available
     */
    fun getFilePath(fileName: String): String?

    /**
     * Check if storage permission is required for write operations.
     * For MediaStore (API 29+) returns false - no permission needed.
     * For Legacy storage (API < 29) returns true - WRITE_EXTERNAL_STORAGE needed.
     * @return true if permission is required
     */
    fun isPermissionRequired(): Boolean

}

