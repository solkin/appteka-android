<p align="center">
  <img src="app/src/main/ic_launcher-web.png" alt="Appteka Logo" width="120" height="120">
</p>

<h1 align="center">Appteka</h1>

<p align="center">
  <strong>Alternative Android App Store</strong>
</p>

<p align="center">
  <a href="https://appteka.store"><img src="https://img.shields.io/badge/dynamic/json?color=32A304&label=Appteka&query=%24.newer.ver_name&url=https%3A%2F%2Fappteka.store%2F%2Fapi%2F1%2Fupdate%3Fbuild%3D1" alt="Appteka"></a>
  <a href="https://apt.izzysoft.de/packages/com.tomclaw.appsend"><img src="https://img.shields.io/endpoint?url=https://apt.izzysoft.de/fdroid/api/v1/shield/com.tomclaw.appsend&label=IzzyOnDroid" alt="IzzyOnDroid"></a>
  <a href="LICENSE.txt"><img src="https://img.shields.io/badge/License-GPL%20v3-blue.svg" alt="License: GPL v3"></a>
  <img src="https://img.shields.io/badge/API-21%2B-brightgreen.svg" alt="API 21+">
  <img src="https://img.shields.io/badge/Kotlin-1.9-purple.svg" alt="Kotlin">
</p>

---

**Appteka** is a free, open-source Android app store where users can discover, download, and share applications. Upload your own apps, explore creations from developers worldwide, and engage with the community through real-time discussions.

## Screenshots

<p align="center">
  <img src="art/main.jpg" alt="Store" width="200">
  <img src="art/app.jpg" alt="App Details" width="200">
  <img src="art/profile.jpg" alt="Profile" width="200">
  <img src="art/topics.jpg" alt="Discussions" width="200">
</p>

## Features

- **Browse & Download** — Explore hundreds of thousands of free Android apps
- **Upload Apps** — Share your applications with the community
- **APK Extractor** — Extract APKs from installed apps (including system apps)
- **Real-time Chat** — Discuss apps and games with other users
- **User Profiles** — Track uploads, downloads, and activity
- **Ratings & Reviews** — Rate apps and read community feedback
- **Favorites** — Save apps for later
- **Dark Theme** — Full dark mode support
- **No Root Required** — Works on any Android device

## Download

<a href="https://appteka.store"><img src="https://img.shields.io/badge/Appteka-Store-32A304?style=for-the-badge&logo=android" alt="Get it on Appteka"></a>
<a href="https://apt.izzysoft.de/packages/com.tomclaw.appsend"><img src="https://shields.rbtlog.dev/simple/com.tomclaw.appsend?style=for-the-badge" alt="Get it on IzzyOnDroid"></a>

Or download the latest APK from the [Releases](https://github.com/solkin/appteka-android/releases) page.

## Tech Stack

| Category | Technologies |
|----------|-------------|
| **Language** | Kotlin |
| **UI** | Material Design 3, AndroidX |
| **Architecture** | MVP (Model-View-Presenter), Clean Architecture |
| **Dependency Injection** | Dagger 2 |
| **Reactive** | RxJava 3, RxKotlin, RxRelay |
| **Networking** | Retrofit 3, OkHttp 5 |
| **Image Loading** | Simple Image Loader |
| **RecyclerView** | Konveyor |
| **Build** | Gradle, ProGuard |

## Architecture

The project follows **Clean Architecture** principles with MVP pattern:

- **Activity/Fragment** — Acts as a router, implements navigation interface
- **Presenter** — Presentation layer, framework-agnostic for easy testing
- **View** — Pure UI rendering, no business logic
- **Interactor** — Business logic, repository pattern, data caching
- **Converter** — Data mapping between layers
- **ResourceProvider** — Android resources access for framework-agnostic layers

## Building

### Requirements

- Android Studio Ladybug or newer
- JDK 17
- Android SDK with API 35

### Build & Run

```bash
# Clone the repository
git clone https://github.com/solkin/appteka-android.git
cd appteka-android

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
./gradlew assembleRelease
```

The APK will be generated at `app/build/outputs/apk/`

## Localization

Appteka is available in **9 languages**:

| Language | Code |
|----------|------|
| English | `en` (default) |
| Russian | `ru` |
| Arabic | `ar` |
| Chinese | `zh` |
| Farsi | `fa` |
| Hindi | `hi` |
| Kurdish | `ku` |
| Portuguese (Brazil) | `pt-rBR` |
| Vietnamese | `vi` |

Want to help translate? Contributions are welcome!

## Contributing

Contributions are welcome! Feel free to:

- Report bugs and request features via [Issues](https://github.com/solkin/appteka-android/issues)
- Submit pull requests with improvements
- Help with translations
- Improve documentation

## Security & Disclaimer

All uploaded applications are automatically scanned by a built-in antivirus system powered by **three independent antivirus engines**.

However, Appteka is a community-driven app exchange platform where users can freely upload applications. **The Appteka team is not responsible for user-generated content.** While security scanning and content moderation are in place, please take appropriate safety precautions — verify the source, check reviews and ratings before installing.

- [Terms of Service](https://appteka.store/legal)
- [DMCA Policy](https://appteka.store/dmca)
- [Contacts & Donations](https://appteka.store/contacts)

## License

```
Copyright (C) 2016-2026 Appteka

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.
```

See [LICENSE.txt](LICENSE.txt) for the full license text.

---

<p align="center">
  Night-coded by Appteka team 🌙
</p>
