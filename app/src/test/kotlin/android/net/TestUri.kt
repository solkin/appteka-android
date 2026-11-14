package android.net

import android.os.Parcel

// Uri has a package-private constructor and no working factory in local JVM tests.
class TestUri : Uri() {

    override fun toString(): String = "content://appteka/test.apk"

    override fun buildUpon(): Builder = error("Not used")
    override fun getAuthority(): String = error("Not used")
    override fun getEncodedAuthority(): String = error("Not used")
    override fun getEncodedFragment(): String = error("Not used")
    override fun getEncodedPath(): String = error("Not used")
    override fun getEncodedQuery(): String = error("Not used")
    override fun getEncodedSchemeSpecificPart(): String = error("Not used")
    override fun getEncodedUserInfo(): String = error("Not used")
    override fun getFragment(): String = error("Not used")
    override fun getHost(): String = error("Not used")
    override fun getLastPathSegment(): String = error("Not used")
    override fun getPath(): String = error("Not used")
    override fun getPathSegments(): List<String> = error("Not used")
    override fun getPort(): Int = error("Not used")
    override fun getQuery(): String = error("Not used")
    override fun getScheme(): String = error("Not used")
    override fun getSchemeSpecificPart(): String = error("Not used")
    override fun getUserInfo(): String = error("Not used")
    override fun isHierarchical(): Boolean = error("Not used")
    override fun isRelative(): Boolean = error("Not used")
    override fun describeContents(): Int = error("Not used")
    override fun writeToParcel(dest: Parcel, flags: Int): Unit = error("Not used")

}
