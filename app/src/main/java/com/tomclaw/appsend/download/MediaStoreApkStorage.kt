package com.tomclaw.appsend.download

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * APK storage implementation for Android 10 (API 29) and above.
 * Stores files in Download/Appteka directory using MediaStore API.
 * Returns content:// URIs that can be used directly for installation.
 */
@RequiresApi(Build.VERSION_CODES.Q)
class MediaStoreApkStorage(
    private val context: Context
) : ApkStorage {

    private val contentResolver get() = context.contentResolver

    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR).apply { mkdirs() }
    }

    /**
     * Session-only URI cache. Stores URIs from insert() to avoid query delays.
     * - Populated in openWrite() and openAppend()
     * - Used in commit() and getInstallUri()
     * - Cleared in delete(), deleteTmp(), clearAll()
     * - Lost on app restart (intentionally - MediaStore will be indexed by then)
     */
    private val sessionUris = mutableMapOf<String, Uri>()

    override fun openWrite(fileName: String): OutputStream {
        deleteTmp(fileName)

        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "$fileName.$APK_EXTENSION.$TMP_EXTENSION")
            put(MediaStore.Downloads.MIME_TYPE, TMP_MIME_TYPE)
            put(MediaStore.Downloads.RELATIVE_PATH, RELATIVE_PATH)
        }

        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IllegalStateException("Failed to create file in MediaStore")

        sessionUris[fileName] = uri

        return contentResolver.openOutputStream(uri)
            ?: throw IllegalStateException("Failed to open output stream")
    }

    override fun commit(fileName: String): Boolean {
        // Use cached URI first (from openWrite/openAppend), fallback to query (for resume after restart)
        val uri = sessionUris[fileName]
            ?: findFileUri("$fileName.$APK_EXTENSION.$TMP_EXTENSION")
            ?: return false

        delete(fileName)

        val updateValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "$fileName.$APK_EXTENSION")
            put(MediaStore.Downloads.MIME_TYPE, APK_MIME_TYPE)
        }

        return try {
            val success = contentResolver.update(uri, updateValues, null, null) > 0
            if (success) {
                // URI remains valid after rename (same ID, different name)
                // Re-add to sessionUris for getInstallUri()
                sessionUris[fileName] = uri
            }
            success
        } catch (ex: Throwable) {
            false
        }
    }

    override fun openRead(fileName: String): InputStream? {
        val uri = sessionUris[fileName] ?: findFileUri("$fileName.$APK_EXTENSION") ?: return null
        return try {
            contentResolver.openInputStream(uri)
        } catch (ex: Throwable) {
            null
        }
    }

    override fun getInstallUri(fileName: String): Uri? {
        // Use cached URI first (same URI valid after commit rename), fallback to query
        val uri = sessionUris[fileName]
            ?: findFileUri("$fileName.$APK_EXTENSION")
            ?: return null

        return try {
            // Verify file is accessible
            contentResolver.openInputStream(uri)?.close()
            uri
        } catch (ex: Throwable) {
            // URI invalid (file deleted externally), clear from cache
            sessionUris.remove(fileName)
            null
        }
    }

    override fun exists(fileName: String): Boolean {
        return sessionUris.containsKey(fileName)
                || findFileUri("$fileName.$APK_EXTENSION") != null
    }

    override fun delete(fileName: String): Boolean {
        sessionUris.remove(fileName)
        File(cacheDir, "$fileName.$APK_EXTENSION").delete()
        val uri = findFileUri("$fileName.$APK_EXTENSION") ?: return false
        return try {
            contentResolver.delete(uri, null, null) > 0
        } catch (ex: Throwable) {
            false
        }
    }

    override fun deleteTmp(fileName: String): Boolean {
        sessionUris.remove(fileName)
        val uri = findFileUri("$fileName.$APK_EXTENSION.$TMP_EXTENSION") ?: return false
        return try {
            contentResolver.delete(uri, null, null) > 0
        } catch (ex: Throwable) {
            false
        }
    }

    override fun getTmpSize(fileName: String): Long {
        val uri = findFileUri("$fileName.$APK_EXTENSION.$TMP_EXTENSION") ?: return 0L

        val projection = arrayOf(MediaStore.Downloads.SIZE)
        return try {
            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads.SIZE))
                } else {
                    0L
                }
            } ?: 0L
        } catch (ex: Throwable) {
            0L
        }
    }

    override fun openAppend(fileName: String): OutputStream {
        val uri = findFileUri("$fileName.$APK_EXTENSION.$TMP_EXTENSION")

        if (uri == null) {
            return openWrite(fileName)
        }

        // Store URI for subsequent commit() call
        sessionUris[fileName] = uri

        return contentResolver.openOutputStream(uri, "wa")
            ?: throw IllegalStateException("Failed to open append stream")
    }

    override fun listApkFiles(): List<ApkInfo> {
        val projection = arrayOf(
            MediaStore.Downloads._ID,
            MediaStore.Downloads.DISPLAY_NAME,
            MediaStore.Downloads.SIZE,
            MediaStore.Downloads.DATE_MODIFIED,
        )
        val selection =
            "${MediaStore.Downloads.RELATIVE_PATH} = ? AND ${MediaStore.Downloads.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf(RELATIVE_PATH, "%.$APK_EXTENSION")

        val result = mutableListOf<ApkInfo>()

        try {
            contentResolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads.SIZE)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(nameColumn)
                    val size = cursor.getLong(sizeColumn)
                    val dateModified = cursor.getLong(dateColumn) * 1000

                    if (displayName.endsWith(".$TMP_EXTENSION")) continue

                    val uri =
                        Uri.withAppendedPath(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            id.toString()
                        )
                    val fileName = displayName.removeSuffix(".$APK_EXTENSION")

                    result.add(
                        ApkInfo(
                            fileName = fileName,
                            uri = uri,
                            size = size,
                            lastModified = dateModified,
                        )
                    )
                }
            }
        } catch (ex: Throwable) {
            // Return empty list on error
        }

        return result
    }

    override fun clearAll(): Int {
        sessionUris.clear()
        cacheDir.listFiles()?.forEach { it.delete() }

        val selection =
            "${MediaStore.Downloads.RELATIVE_PATH} = ? AND ${MediaStore.Downloads.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf(RELATIVE_PATH, "%.$APK_EXTENSION")

        return try {
            contentResolver.delete(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                selection,
                selectionArgs
            )
        } catch (ex: Throwable) {
            0
        }
    }

    override fun copyToStorage(input: InputStream, fileName: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "$fileName.$APK_EXTENSION")
            put(MediaStore.Downloads.MIME_TYPE, APK_MIME_TYPE)
            put(MediaStore.Downloads.RELATIVE_PATH, RELATIVE_PATH)
        }

        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return null

        return try {
            contentResolver.openOutputStream(uri)?.use { output ->
                input.copyTo(output)
                output.flush()
            }
            uri
        } catch (ex: Throwable) {
            contentResolver.delete(uri, null, null)
            null
        }
    }

    override fun getFilePath(fileName: String): String? {
        val uri = sessionUris[fileName] ?: findFileUri("$fileName.$APK_EXTENSION") ?: return null

        val cachedFile = File(cacheDir, "$fileName.$APK_EXTENSION")
        if (cachedFile.exists()) {
            return cachedFile.absolutePath
        }

        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                cachedFile.outputStream().use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }
            cachedFile.absolutePath
        } catch (ex: Throwable) {
            null
        }
    }

    override fun isPermissionRequired(): Boolean = false

    private fun findFileUri(displayName: String): Uri? {
        val projection = arrayOf(MediaStore.Downloads._ID)
        val selection =
            "${MediaStore.Downloads.DISPLAY_NAME} = ? AND ${MediaStore.Downloads.RELATIVE_PATH} = ?"
        val selectionArgs = arrayOf(displayName, RELATIVE_PATH)

        return try {
            contentResolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                    Uri.withAppendedPath(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id.toString())
                } else {
                    null
                }
            }
        } catch (ex: Throwable) {
            null
        }
    }

}

private const val APPTEKA_DIR = "Appteka"
private const val RELATIVE_PATH = "Download/$APPTEKA_DIR/"
private const val CACHE_DIR = "apk_cache"
private const val APK_EXTENSION = "apk"
private const val TMP_EXTENSION = "tmp"
private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
private const val TMP_MIME_TYPE = "application/octet-stream"
