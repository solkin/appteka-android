package com.tomclaw.appsend.download

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
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

    // Cache directory for temporary file access (for PackageManager)
    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR).apply { mkdirs() }
    }

    // Store pending URIs to use in commit() - more reliable than querying
    private val pendingUris = mutableMapOf<String, Uri>()
    
    // Store committed URIs for immediate access after commit (MediaStore indexing is async)
    private val committedUris = mutableMapOf<String, Uri>()

    override fun openWrite(fileName: String): OutputStream {
        // Delete existing files first
        deleteTmp(fileName)

        val displayName = "$fileName.$APK_EXTENSION.tmp"
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, displayName)
            put(MediaStore.Downloads.MIME_TYPE, TMP_MIME_TYPE)
            put(MediaStore.Downloads.RELATIVE_PATH, "$DOWNLOAD_DIR/$APPTEKA_DIR/")
            // Don't use IS_PENDING for tmp files - they need to survive app restart for resume
        }

        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IllegalStateException("Failed to create file in MediaStore")

        // Store URI for later use in commit()
        pendingUris[fileName] = uri

        return contentResolver.openOutputStream(uri)
            ?: throw IllegalStateException("Failed to open output stream for $uri")
    }

    override fun commit(fileName: String): Boolean {
        // Use stored URI (more reliable than querying)
        val tmpUri = pendingUris.remove(fileName)

        if (tmpUri == null) {
            // Fallback to query if URI not found in cache (e.g., app restart during download)
            val tmpName = "$fileName.$APK_EXTENSION.tmp"
            val foundUri = findFileUri(tmpName) ?: return false
            return commitUri(foundUri, fileName)
        }

        return commitUri(tmpUri, fileName)
    }

    private fun commitUri(uri: Uri, fileName: String): Boolean {
        val finalName = "$fileName.$APK_EXTENSION"
        val updateValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, finalName)
            put(MediaStore.Downloads.MIME_TYPE, APK_MIME_TYPE)
        }

        // Delete existing target file if exists
        delete(fileName)

        return try {
            val updated = contentResolver.update(uri, updateValues, null, null)
            if (updated > 0) {
                // Cache URI for immediate access
                committedUris[fileName] = uri
                // Wait for MediaStore indexing to complete (async operation)
                // Verify file is accessible before returning success
                waitForFileAccess(uri)
            }
            updated > 0
        } catch (ex: Throwable) {
            false
        }
    }

    private fun waitForFileAccess(uri: Uri) {
        repeat(INDEXING_RETRY_COUNT) { attempt ->
            try {
                contentResolver.openInputStream(uri)?.close()
                return
            } catch (ex: SecurityException) {
                if (attempt < INDEXING_RETRY_COUNT - 1) {
                    Thread.sleep(INDEXING_RETRY_DELAY_MS)
                }
            }
        }
    }

    override fun openRead(fileName: String): InputStream? {
        val uri = findFileUri("$fileName.$APK_EXTENSION") ?: return null
        return contentResolver.openInputStream(uri)
    }

    override fun getInstallUri(fileName: String): Uri? {
        // First check cached URI (MediaStore indexing is async, query may fail right after commit)
        val uri = committedUris[fileName] ?: findFileUri("$fileName.$APK_EXTENSION")
        if (uri == null) return null

        // Verify file is accessible before returning URI
        // MediaStore indexing is async, file may not be ready immediately after commit
        return try {
            contentResolver.openInputStream(uri)?.close()
            uri
        } catch (ex: Throwable) {
            // File not accessible (SecurityException) or deleted (FileNotFoundException)
            // Clear from cache and return null, next call will retry
            committedUris.remove(fileName)
            null
        }
    }

    override fun exists(fileName: String): Boolean {
        // Check cached URI first (MediaStore indexing is async)
        if (committedUris.containsKey(fileName)) {
            return true
        }
        return findFileUri("$fileName.$APK_EXTENSION") != null
    }

    override fun delete(fileName: String): Boolean {
        // Remove from committed URIs cache
        committedUris.remove(fileName)
        
        // Also delete cached copy
        File(cacheDir, "$fileName.$APK_EXTENSION").delete()

        val uri = findFileUri("$fileName.$APK_EXTENSION") ?: return false
        return contentResolver.delete(uri, null, null) > 0
    }

    override fun deleteTmp(fileName: String): Boolean {
        // Try stored URI first
        val storedUri = pendingUris.remove(fileName)
        if (storedUri != null) {
            return contentResolver.delete(storedUri, null, null) > 0
        }
        // Fallback to query (tmp files are not pending, so no need for includePending)
        val uri = findFileUri("$fileName.$APK_EXTENSION.tmp") ?: return false
        return contentResolver.delete(uri, null, null) > 0
    }

    override fun getTmpSize(fileName: String): Long {
        // Tmp files are not pending, so no need for includePending
        val uri = findFileUri("$fileName.$APK_EXTENSION.tmp")
            ?: return 0L

        val projection = arrayOf(MediaStore.Downloads.SIZE)
        return contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads.SIZE))
            } else {
                0L
            }
        } ?: 0L
    }

    override fun openAppend(fileName: String): OutputStream {
        // Tmp files are not pending, so no need for includePending
        val uri = findFileUri("$fileName.$APK_EXTENSION.tmp")
        
        if (uri == null) {
            // No existing tmp file, create new one
            return openWrite(fileName)
        }
        
        // Store URI for later use in commit()
        pendingUris[fileName] = uri
        
        // Open in write-append mode
        return contentResolver.openOutputStream(uri, "wa")
            ?: throw IllegalStateException("Failed to open append stream for $uri")
    }

    override fun listApkFiles(): List<ApkInfo> {
        val projection = arrayOf(
            MediaStore.Downloads._ID,
            MediaStore.Downloads.DISPLAY_NAME,
            MediaStore.Downloads.SIZE,
            MediaStore.Downloads.DATE_MODIFIED,
        )
        val selection = "${MediaStore.Downloads.RELATIVE_PATH} = ? AND ${MediaStore.Downloads.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("$DOWNLOAD_DIR/$APPTEKA_DIR/", "%.$APK_EXTENSION")

        val result = mutableListOf<ApkInfo>()

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
                val dateModified = cursor.getLong(dateColumn) * 1000 // Convert to millis

                // Skip tmp files
                if (displayName.endsWith(".tmp")) continue

                val uri = Uri.withAppendedPath(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id.toString())
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

        return result
    }

    override fun clearAll(): Int {
        val selection = "${MediaStore.Downloads.RELATIVE_PATH} = ? AND ${MediaStore.Downloads.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("$DOWNLOAD_DIR/$APPTEKA_DIR/", "%.$APK_EXTENSION")

        // Clear committed URIs cache
        committedUris.clear()
        
        // Clear cache directory
        cacheDir.listFiles()?.forEach { it.delete() }

        return contentResolver.delete(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            selection,
            selectionArgs
        )
    }

    override fun copyToStorage(input: InputStream, fileName: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "$fileName.$APK_EXTENSION")
            put(MediaStore.Downloads.MIME_TYPE, APK_MIME_TYPE)
            put(MediaStore.Downloads.RELATIVE_PATH, "$DOWNLOAD_DIR/$APPTEKA_DIR/")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return null

        return try {
            contentResolver.openOutputStream(uri)?.use { output ->
                input.copyTo(output)
                output.flush()
            }

            val updateValues = ContentValues().apply {
                put(MediaStore.Downloads.IS_PENDING, 0)
            }
            contentResolver.update(uri, updateValues, null, null)

            uri
        } catch (ex: Throwable) {
            contentResolver.delete(uri, null, null)
            null
        }
    }

    override fun getFilePath(fileName: String): String? {
        val uri = findFileUri("$fileName.$APK_EXTENSION") ?: return null

        // Copy to cache for file path access
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

    @Suppress("DEPRECATION")
    private fun findFileUri(displayName: String, includePending: Boolean = false): Uri? {
        val projection = arrayOf(MediaStore.Downloads._ID)
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ? AND ${MediaStore.Downloads.RELATIVE_PATH} = ?"
        val relativePath = "$DOWNLOAD_DIR/$APPTEKA_DIR/"
        val selectionArgs = arrayOf(displayName, relativePath)

        // Use appropriate query URI based on API level
        val queryUri = if (includePending) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // For API 30+, use Bundle with QUERY_ARG_MATCH_PENDING
                val queryArgs = Bundle().apply {
                    putString(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                    putStringArray(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
                    putInt(MediaStore.QUERY_ARG_MATCH_PENDING, MediaStore.MATCH_INCLUDE)
                }
                contentResolver.query(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    projection,
                    queryArgs,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                        return Uri.withAppendedPath(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id.toString())
                    }
                }
                return null
            } else {
                // For API 29, use setIncludePending (deprecated but necessary)
                MediaStore.setIncludePending(MediaStore.Downloads.EXTERNAL_CONTENT_URI)
            }
        } else {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        }

        contentResolver.query(
            queryUri,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                return Uri.withAppendedPath(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id.toString())
            }
        }
        return null
    }

}

private const val APPTEKA_DIR = "Appteka"
private const val DOWNLOAD_DIR = "Download"
private const val CACHE_DIR = "apk_cache"
private const val APK_EXTENSION = "apk"
private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
private const val TMP_MIME_TYPE = "application/octet-stream"
private const val INDEXING_RETRY_COUNT = 8
private const val INDEXING_RETRY_DELAY_MS = 250L
