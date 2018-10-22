Support PreferenceFragment
=====================================

Unofficial PreferenceFragment compatibility layer for Android 1.6 and up.

###About the status of the library
This project is not abandoned, i just haven't had time to spare for it; I accept pull-requests and all the good Github open-source stuff though. 

It is in my plans to actually give it the holo love it deserves [soon](http://www.wowwiki.com/Soon) (tm).

How to reference this library?
====================================

Make sure you have maven central listed as a repository on your build.gradle like this:

```groovy
repositories {
    mavenCentral()
}
```

Add the dependency to your build.gradle file like this:

```groovy
compile 'com.github.machinarius:preferencefragment:0.1.1'
```
The fix has been deployed, no more @aar suffix

How to use it?
===================================

Just extend PreferenceFragment and follow the Settings developer guide like if this layer wasn't even there to begin with.

http://developer.android.com/guide/topics/ui/settings.html

Roadmap
==================================
- Bump the presentation of the settings on Gingerbread and lower with some Holo love
- Include a handful of useful extra preferences
