# 🍌 Bananalytics

Lightweight analytics and crash reporting library for Android.

## Features

- **Event tracking** — custom events with tags and numeric fields
- **Crash reporting** — automatic fatal crash capture + manual non-fatal exception tracking
- **Breadcrumbs** — contextual trail of user actions before a crash
- **Offline-first** — events/crashes stored locally, sent when network available
- **Self-contained** — module handles network requests internally
- **Minimal dependencies** — Kotlin stdlib, Gson, OkHttp

## Quick Start

```kotlin
// 1. Configure Bananalytics
val config = BananalyticsConfig(
    baseUrl = "https://banana.appteka.store",
    apiKey = "bnn_xxxxx"
)

// 2. Implement EnvironmentProvider
class MyEnvironmentProvider : EnvironmentProvider {
    override fun environment() = Environment(
        packageName = "com.example.app",
        appVersion = 123,
        deviceId = "uuid-string",
        osVersion = Build.VERSION.SDK_INT,
        manufacturer = Build.MANUFACTURER,
        model = Build.MODEL,
        country = Locale.getDefault().country,
        language = Locale.getDefault().language
    )
}

// 3. Create instance
val bananalytics = BananalyticsImpl(
    filesDir = context.filesDir,
    config = config,
    environmentProvider = MyEnvironmentProvider(),
    isDebug = BuildConfig.DEBUG
)

// 4. Install (call early in Application.onCreate)
bananalytics.install()
```

## Configuration

```kotlin
data class BananalyticsConfig(
    val baseUrl: String,  // Base URL (e.g., "https://banana.appteka.store")
    val apiKey: String,   // API key (format: "bnn_xxxxx")
)
```

## API

```kotlin
interface Bananalytics {
    fun install()
    fun trackEvent(name: String, tags: Map<String, String>, fields: Map<String, Double>)
    fun trackException(throwable: Throwable, context: Map<String, String>)
    fun leaveBreadcrumb(message: String, category: BreadcrumbCategory)
    fun flushEvents()
}
```

## How It Works

### Event Flow

```
trackEvent() ──► JSON file ──► batch (20 events) ──► POST /events/submit
                  └── stored in files/bananalytics/events/
```

### Crash Flow

```
UncaughtException ──► JSON file (sync write) ──► app dies
                        └── stored in files/bananalytics/crashes/

Next app launch ──► install() ──► send pending crashes ──► POST /crashes/submit
```

### Breadcrumbs

- Stored in memory (ring buffer, max 50 items)
- Attached to crash report when crash occurs
- Categories: `NAVIGATION`, `USER_ACTION`, `NETWORK`, `ERROR`, `CUSTOM`

## Local Storage

```
files/bananalytics/
├── events/
│   ├── 1704067200000-a1b2c3.event    # JSON: AnalyticsEvent
│   └── 1704067201000-d4e5f6.event
└── crashes/
    └── 1704067199000-fatal.crash      # JSON: CrashReport
```

---

# Backend API Contract

All requests require authentication via `X-API-Key` header.

## POST `/api/v1/events/submit`

Submit analytics events.

**Headers:**
```
X-API-Key: bnn_xxxxx
Content-Type: application/json
```

**Request:**
```json
{
  "session_id": "550e8400-e29b-41d4-a716-446655440000",
  "environment": {
    "package_name": "com.example.app",
    "app_version": 123,
    "app_version_name": "1.2.3",
    "device_id": "uuid-string",
    "os_version": 34,
    "manufacturer": "Google",
    "model": "Pixel 7",
    "country": "US",
    "language": "en"
  },
  "events": [
    {
      "session_id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "button_click",
      "tags": {
        "screen": "home"
      },
      "fields": {
        "load_time": 1.5
      },
      "time": 1704067200000
    }
  ]
}
```

**Response:** `200 OK`
```json
{
  "status": 200
}
```

---

## POST `/api/v1/crashes/submit`

Submit crash reports.

**Headers:**
```
X-API-Key: bnn_xxxxx
Content-Type: application/json
```

**Request:**
```json
{
  "session_id": "550e8400-e29b-41d4-a716-446655440000",
  "environment": {
    "package_name": "com.example.app",
    "app_version": 123,
    "app_version_name": "1.2.3",
    "device_id": "uuid-string",
    "os_version": 34,
    "manufacturer": "Google",
    "model": "Pixel 7",
    "country": "US",
    "language": "en"
  },
  "crashes": [
    {
      "session_id": "550e8400-e29b-41d4-a716-446655440000",
      "timestamp": 1704067199000,
      "thread": "main",
      "stacktrace": "java.lang.NullPointerException: Attempt to invoke...",
      "is_fatal": true,
      "context": {
        "screen": "details"
      },
      "breadcrumbs": [
        {
          "timestamp": 1704067190000,
          "message": "HomeActivity",
          "category": "navigation"
        }
      ]
    }
  ]
}
```

**Response:** `200 OK`
```json
{
  "status": 200
}
```

---

## Data Models

### Environment

| Field | Type | Description |
|-------|------|-------------|
| `package_name` | string | Application package name |
| `app_version` | long | Version code |
| `app_version_name` | string | Version name (e.g., "1.2.3") |
| `device_id` | string | Unique device identifier |
| `os_version` | int | Android SDK version |
| `manufacturer` | string | Device manufacturer |
| `model` | string | Device model |
| `country` | string | ISO country code |
| `language` | string | ISO language code |

### AnalyticsEvent

| Field | Type | Description |
|-------|------|-------------|
| `session_id` | string | Session UUID when event occurred |
| `name` | string | Event name (underscores, no dashes) |
| `tags` | Map<string, string> | String key-value pairs |
| `fields` | Map<string, double> | Numeric key-value pairs |
| `time` | long | Unix timestamp (ms) |

### CrashReport

| Field | Type | Description |
|-------|------|-------------|
| `session_id` | string | Session UUID when crash occurred |
| `timestamp` | long | Unix timestamp (ms) |
| `thread` | string | Thread name where crash occurred |
| `stacktrace` | string | Full stack trace |
| `is_fatal` | boolean | `true` for uncaught exceptions |
| `context` | Map<string, string> | Additional context |
| `breadcrumbs` | List<Breadcrumb> | Actions before crash |

### Breadcrumb

| Field | Type | Description |
|-------|------|-------------|
| `timestamp` | long | Unix timestamp (ms) |
| `message` | string | Description |
| `category` | string | `navigation`, `user_action`, `network`, `error`, `custom` |

---

## Reliability Guarantees

1. **Crash handler writes synchronously** — no background threads, data saved before app dies
2. **Chain preservation** — default handler called after our handler
3. **Fail-safe** — exceptions in crash handler are caught, never cause secondary crash
4. **Retry on network failure** — data stays on disk until successfully sent
5. **Batch processing** — events sent in batches of 20 for efficiency

## License

Apache 2.0
