BDUI framework regression checks

Requires JDK 17+, Android platform 37, build-tools 36.0.0, and a debug APK signed
with the default local debug key. No additional test libraries are required.

Serve flipper-state.json from the local test backend, configure the app to use
that backend, and pass its device-reachable URL. The schema and app API must be
accessible from the emulator; production data is not needed.

JAVA_HOME=... ANDROID_HOME=... python3 qa/bdui/run.py --device emulator-5582 \
  --apk app/build/outputs/apk/debug/appteka-24.0-1197.apk \
  --url https://LOCAL_TEST_HOST/qa-fixtures/flipper-state.json

The runner checks initial and changed intervals, independent components, and
actual automatic flipping. Run on API 23-29 and a current Android version.
For toolbar icon tint checks, serve menu-tint.json and pass --mode menu. It checks
separate action/overflow tint colors, absent/invalid tint, and menu action delivery.
Run that case on API 23-26 and a current Android version, in light and dark themes.
Reflection calls the app's public BDUI entry points from the standalone test APK;
production code does not use reflection or an API-dependent fallback getter.
A nonzero exit status means the native assertions or a build/install step failed.
