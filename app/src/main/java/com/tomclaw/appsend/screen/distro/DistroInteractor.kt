package com.tomclaw.appsend.screen.distro

import android.os.Environment
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

interface DistroInteractor {

    fun listDistroApps(): Observable<List<DistroAppEntity>>

    fun getPackagePermissions(path: String): List<String>

    fun getPackageUploadInfo(packageName: String): Pair<UploadPackage, UploadApk>?

    fun removeApk(path: String): Observable<Unit>

    fun saveApkToDownloads(path: String): Single<String> // Function to save APK to Downloads/App folder
}

class DistroInteractorImpl(
    private val infoProvider: DistroInfoProvider,
    private val schedulers: SchedulersFactory
) : DistroInteractor {

    override fun listDistroApps(): Observable<List<DistroAppEntity>> {
        return Single
            .create {
                val items = infoProvider.getApkItems()
                it.onSuccess(items)
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun getPackagePermissions(path: String): List<String> {
        return infoProvider.getPackagePermissions(path)
    }

    override fun getPackageUploadInfo(packageName: String): Pair<UploadPackage, UploadApk>? {
        return infoProvider.getPackageUploadInfo(packageName)
    }

    override fun removeApk(path: String): Observable<Unit> {
        return Single
            .create {
                File(path).delete()
                it.onSuccess(Unit)
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    // Implementation for saving the APK file to the public Downloads/App folder
    override fun saveApkToDownloads(path: String): Single<String> {
        return Single
            .create { emitter ->
                val sourceFile = File(path)
                
                // Define the destination directory: Downloads/App
                val destDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "App")
                if (!destDir.exists()) {
                    destDir.mkdirs() // Create directory if it does not exist
                }
                
                val destFile = File(destDir, sourceFile.name)
                
                try {
                    // Copy file using channels for efficiency
                    FileInputStream(sourceFile).channel.use { source ->
                        FileOutputStream(destFile).channel.use { destination ->
                            destination.transferFrom(source, 0, source.size())
                        }
                    }
                    emitter.onSuccess(destFile.absolutePath) // Return the absolute destination path
                } catch (e: Exception) {
                    emitter.onError(IOException("Failed to copy file: ${e.message}", e)) // Handle file copy error
                }
            }
            .subscribeOn(schedulers.io()) // Run file operation on IO thread
    }
}
