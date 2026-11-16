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
import java.util.concurrent.ConcurrentHashMap

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
     * Session-only URI caches, kept to avoid a MediaStore query per lookup and
     * lost on app restart (MediaStore will be indexed by then).
     *
     * The half-written file and the finished one are deliberately kept apart:
     * a single cache made a download in progress answer exists() and hand its
     * partial URI to the installer, which then failed to parse the package.
     *
     * Written from the download executor and read from the main thread, so they
     * have to be concurrent the same way DownloadManager's own maps are.
     */
    private val tmpUris = ConcurrentHashMap<String, Uri>()
    private val apkUris = ConcurrentHashMap<String, Uri>()

    override fun openWrite(fileName: String): OutputStream {
        deleteTmp(fileName)

        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "$fileName.$APK_EXTENSION.$TMP_EXTENSION")
            put(MediaStore.Downloads.MIME_TYPE, TMP_MIME_TYPE)
            put(MediaStore.Downloads.RELATIVE_PATH, RELATIVE_PATH)
        }

        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IllegalStateException("Failed to create file in MediaStore")

        tmpUris[fileName] = uri

        return contentResolver.openOutputStream(uri)
            ?: throw IllegalStateException("Failed to open output stream")
    }

    override fun commit(fileName: String): Uri? {
        // Use cached URI first (from openWrite/openAppend), fallback to query (for resume after restart)
        val uri = tmpUris[fileName]
            ?: findFileUri("$fileName.$APK_EXTENSION.$TMP_EXTENSION")
            ?: return null

        delete(fileName)

        val updateValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "$fileName.$APK_EXTENSION")
            put(MediaStore.Downloads.MIME_TYPE, APK_MIME_TYPE)
        }

        return try {
            if (contentResolver.update(uri, updateValues, null, null) > 0) {
                // The URI survives the rename (same id, different name), so it
                // moves over to the finished side rather than being re-queried
                tmpUris.remove(fileName)
                apkUris[fileName] = uri
                uri
            } else {
                null
            }
        } catch (ex: Throwable) {
            null
        }
    }

    override fun openRead(fileName: String): InputStream? {
        val uri = apkUris[fileName] ?: findFileUri("$fileName.$APK_EXTENSION") ?: return null
        return try {
            contentResolver.openInputStream(uri)
        } catch (ex: Throwable) {
            null
        }
    }

    override fun getInstallUri(fileName: String): Uri? {
        // Use cached URI first (same URI valid after commit rename), fallback to query
        val cached = apkUris[fileName]
        if (cached != null && isReadable(cached)) {
            return cached
        }

        // A cached URI that won't open is not proof the file is gone — it can
        // just be stale. Reporting "no file" straight away is what left the
        // caller with nothing to install until it asked a second time.
        apkUris.remove(fileName)

        val queried = findFileUri("$fileName.$APK_EXTENSION") ?: return null
        if (!isReadable(queried)) {
            return null
        }
        apkUris[fileName] = queried
        return queried
    }

    private fun isReadable(uri: Uri): Boolean {
        return try {
            contentResolver.openInputStream(uri)?.use { true } ?: false
        } catch (ex: Throwable) {
            false
        }
    }

    // Only a committed file counts, and only while it is still readable — a
    // caller takes a true here as "no need to download, go straight to install"
    override fun exists(fileName: String): Boolean = getInstallUri(fileName) != null

    override fun delete(fileName: String): Boolean {
        apkUris.remove(fileName)
        File(cacheDir, "$fileName.$APK_EXTENSION").delete()
        val uri = findFileUri("$fileName.$APK_EXTENSION") ?: return false
        return try {
            contentResolver.delete(uri, null, null) > 0
        } catch (ex: Throwable) {
            false
        }
    }

    override fun deleteTmp(fileName: String): Boolean {
        tmpUris.remove(fileName)
        val uri = findFileUri("$fileName.$APK_EXTENSION.$TMP_EXTENSION") ?: return false
        return try {
            contentResolver.delete(uri, null, null) > 0
        } catch (ex: Throwable) {
            false
        }
    }

    // Measured on the file itself rather than read from the SIZE column: the
    // column is a database value that can lag behind bytes already on disk
    // (a process killed mid-write), and openAppend writes at the real end of
    // the file — resuming from a stale offset would splice the download.
    override fun getTmpSize(fileName: String): Long {
        val uri = findFileUri("$fileName.$APK_EXTENSION.$TMP_EXTENSION") ?: return 0L

        return try {
            contentResolver.openFileDescriptor(uri, "r")
                ?.use { descriptor -> descriptor.statSize.coerceAtLeast(0L) }
                ?: 0L
        } catch (ex: Throwable) {
            0L
        }
    }

    override fun openReadTmp(fileName: String): InputStream? {
        val uri = tmpUris[fileName]
            ?: findFileUri("$fileName.$APK_EXTENSION.$TMP_EXTENSION")
            ?: return null
        return try {
            contentResolver.openInputStream(uri)
        } catch (ex: Throwable) {
            null
        }
    }

    override fun openAppend(fileName: String): OutputStream {
        val uri = findFileUri("$fileName.$APK_EXTENSION.$TMP_EXTENSION")

        if (uri == null) {
            return openWrite(fileName)
        }

        // Store URI for subsequent commit() call
        tmpUris[fileName] = uri

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
        tmpUris.clear()
        apkUris.clear()
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
        val uri = apkUris[fileName] ?: findFileUri("$fileName.$APK_EXTENSION") ?: return null

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
