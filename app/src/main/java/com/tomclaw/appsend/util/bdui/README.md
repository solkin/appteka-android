# BDUI - Backend-Driven UI

BDUI is a server-driven UI rendering system for Android that allows building dynamic user interfaces from JSON schemas received from a server.

## Table of Contents

- [Overview](#overview)
- [Quick Start](#quick-start)
- [BduiScreenActivity](#bduiscreenactivity)
- [Architecture](#architecture)
- [JSON Schema Specification](#json-schema-specification)
  - [Nodes](#nodes)
  - [Containers](#containers)
  - [Components](#components)
  - [Layout Parameters](#layout-parameters)
  - [Actions](#actions)
  - [Transforms](#transforms)
  - [Refs](#refs)
- [Examples](#examples)

---

## Overview

BDUI enables:
- **Dynamic UI**: Render UI from server-provided JSON without app updates
- **Material 3 Expressive**: Full support for Material 3 components
- **Actions**: Handle user interactions via RPC, callbacks, or transforms
- **Refs**: Dynamic data binding between components
- **Transforms**: Modify UI state without full re-render

---

## Quick Start

### 1. Add BduiView to your layout

```xml
<com.tomclaw.appsend.util.bdui.BduiView
    android:id="@+id/bdui_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### 2. Initialize and render

```kotlin
class MyActivity : AppCompatActivity(), BduiActionListener {

    @Inject
    lateinit var schedulersFactory: SchedulersFactory
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my)
        
        val bduiView = findViewById<BduiView>(R.id.bdui_view)
        bduiView.initialize(schedulersFactory, this)
        
        // Parse and render schema from server
        val schema = BduiJsonParser.parseNode(jsonFromServer)
        bduiView.render(schema)
    }
    
    override fun onCallback(name: String, data: Any?) {
        when (name) {
            "openDetails" -> {
                val id = (data as? Map<*, *>)?.get("id") as? String
                startActivity(DetailsActivity.createIntent(this, id))
            }
        }
    }
    
    override fun onRpcRequest(action: BduiRpcAction): Single<BduiRpcResponse> {
        return api.executeRpc(action.endpoint, action.method, action.payload)
            .map { response -> BduiJsonParser.parseRpcResponse(response) }
    }
}
```

---

## BduiScreenActivity

A ready-to-use Activity that loads and renders BDUI from a remote URL. Provides loading state with Wavy indicator, error handling, and built-in callback support.

### Usage

```kotlin
// Open a dynamic screen
val intent = createBduiScreenActivityIntent(
    context = this,
    url = "https://api.example.com/bdui/promo.json",
    title = "Special Offer"  // Optional toolbar title
)
startActivity(intent)
```

### Features

- **Loading State**: Shows Material 3 Wavy CircularProgressIndicator while loading
- **Error Handling**: Displays error view with retry button on failure
- **Smart Toolbar**: Shows during loading/error, hides when content is displayed (BDUI can provide its own Toolbar component)
- **RPC Support**: Automatically handles RPC actions from the schema
- **Built-in Callbacks**: Common callbacks are handled automatically
- **Route Support**: Navigate to any app screen via route actions

### Built-in Callbacks

| Callback Name | Description | Data |
|---------------|-------------|------|
| `close` | Close the screen | - |
| `back` | Trigger back navigation | - |
| `setResult` | Set activity result | `{ "code": 1 }` |
| `finishWithResult` | Set result and close | `{ "code": 1 }` |

### Extending with Custom Callbacks

Create a subclass to handle custom callbacks:

```kotlin
class PromoActivity : BduiScreenActivity() {
    
    override fun handleCallback(name: String, data: Any?) {
        when (name) {
            "openProduct" -> {
                val productId = (data as? Map<*, *>)?.get("productId") as? String
                startActivity(ProductActivity.createIntent(this, productId))
            }
            "share" -> {
                val text = (data as? Map<*, *>)?.get("text") as? String
                shareText(text)
            }
            else -> super.handleCallback(name, data)
        }
    }
}
```

### HTTP Headers

All HTTP requests in the application automatically include the following headers with app and device information (via `AppInfoInterceptor`):

| Header | Description | Example |
|--------|-------------|---------|
| `X-App-Package` | Application package name | `com.tomclaw.appsend` |
| `X-App-Version-Name` | App version name | `3.2.0` |
| `X-App-Version-Code` | App version code | `320` |
| `X-App-Signature` | SHA-256 hash of APK signature | `a1b2c3d4...` |
| `X-Android-Version` | Android SDK version | `34` |
| `X-Android-Release` | Android release version | `14` |
| `X-Device-Manufacturer` | Device manufacturer | `Samsung` |
| `X-Device-Model` | Device model | `SM-G998B` |
| `X-Locale` | User locale | `en-US` |
| `X-Theme` | Current theme | `dark` or `light` |
| `X-Timezone` | User timezone ID | `Europe/Moscow` |
| `X-Local-Time` | User local time with timezone | `2025-12-27T15:30:00+03:00` |

These headers allow the server to:
- Personalize UI based on app version (e.g., show update prompts)
- Adapt content for device capabilities
- Serve localized content
- Adjust colors for theme compatibility
- Schedule time-sensitive content

### Screen Structure

```
┌─────────────────────────────────────┐
│           Toolbar                   │
├─────────────────────────────────────┤
│                                     │
│    ┌─────────────────────────┐      │
│    │  Loading: Wavy Loader   │      │
│    └─────────────────────────┘      │
│                                     │
│    ┌─────────────────────────┐      │
│    │  Content: BduiView      │      │
│    └─────────────────────────┘      │
│                                     │
│    ┌─────────────────────────┐      │
│    │  Error: Message + Retry │      │
│    └─────────────────────────┘      │
│                                     │
└─────────────────────────────────────┘
```

### Example Server Response

```json
{
  "id": "promo_screen",
  "type": "scroll",
  "children": [
    {
      "id": "content",
      "type": "linear",
      "orientation": "vertical",
      "layoutParams": { "padding": { "all": 16 } },
      "children": [
        {
          "id": "banner",
          "type": "image",
          "src": "https://example.com/promo-banner.jpg",
          "imageStyle": { "cornerRadius": 16 },
          "layoutParams": { 
            "width": "match_parent",
            "height": "200dp"
          }
        },
        {
          "id": "title",
          "type": "text",
          "text": "Summer Sale!",
          "textStyle": { 
            "textSize": 24, 
            "fontWeight": "bold",
            "textAlign": "center" 
          },
          "layoutParams": { "margin": { "top": 16, "bottom": 8 } }
        },
        {
          "id": "description",
          "type": "text",
          "text": "Get 50% off on all items. Limited time offer!",
          "textStyle": { "textAlign": "center" },
          "layoutParams": { "margin": { "bottom": 24 } }
        },
        {
          "id": "cta_button",
          "type": "button",
          "text": "Shop Now",
          "variant": "primary",
          "layoutParams": { "width": "match_parent" },
          "action": {
            "type": "callback",
            "name": "openProduct",
            "data": { "productId": "summer-sale" }
          }
        },
        {
          "id": "dismiss_button",
          "type": "button",
          "text": "Maybe Later",
          "variant": "text",
          "layoutParams": { 
            "width": "match_parent",
            "margin": { "top": 8 } 
          },
          "action": {
            "type": "callback",
            "name": "close"
          }
        }
      ]
    }
  ]
}
```

### File Structure

```
screen/bdui/
├── BduiScreenActivity.kt       # Activity with router
├── BduiScreenPresenter.kt      # Loading logic
├── BduiScreenView.kt           # View interface and implementation
├── BduiScreenInteractor.kt     # Network requests
└── di/
    ├── BduiScreenComponent.kt  # Dagger component
    └── BduiScreenModule.kt     # Dagger module
```

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         BduiView                             │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                     BduiRenderer                         │ │
│  │  ┌─────────────────┐  ┌─────────────────────────────┐  │ │
│  │  │ContainerFactory │  │     ComponentFactory        │  │ │
│  │  └─────────────────┘  └─────────────────────────────┘  │ │
│  └─────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                   BduiActionHandler                      │ │
│  │  ┌──────────┐  ┌───────────────┐  ┌─────────────────┐  │ │
│  │  │RefResolver│  │TransformHandler│  │  RPC/Callback   │  │ │
│  │  └──────────┘  └───────────────┘  └─────────────────┘  │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## JSON Schema Specification

### Nodes

Every node has these common fields:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | string | ✅ | Unique identifier for the node |
| `type` | string | ✅ | Node type (container or component) |
| `layoutParams` | object | ❌ | Layout configuration |
| `action` | object | ❌ | Action to execute on interaction |

---

### Containers

#### Frame Container (`type: "frame"`)

Stacks children on top of each other.

```json
{
  "id": "container1",
  "type": "frame",
  "children": [...]
}
```

#### Linear Container (`type: "linear"`)

Arranges children in a row or column.

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `orientation` | string | `"vertical"` | `"vertical"` or `"horizontal"` |
| `gravity` | string | - | Content gravity |
| `weightSum` | number | - | Total weight for children |
| `children` | array | - | Child nodes |

```json
{
  "id": "container1",
  "type": "linear",
  "orientation": "vertical",
  "gravity": "center",
  "children": [...]
}
```

#### Recycler Container (`type: "recycler"`)

Scrollable list/grid of items.

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `orientation` | string | `"vertical"` | Scroll direction |
| `spanCount` | number | `1` | Columns for grid layout |
| `reverseLayout` | boolean | `false` | Reverse item order |
| `itemSpacing` | number | - | Spacing between items (dp) |
| `children` | array | - | Item nodes |

```json
{
  "id": "list1",
  "type": "recycler",
  "orientation": "vertical",
  "spanCount": 2,
  "itemSpacing": 8,
  "children": [...]
}
```

#### Scroll Container (`type: "scroll"`)

Provides scrolling for content.

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `orientation` | string | `"vertical"` | Scroll direction |
| `fillViewport` | boolean | `false` | Fill viewport |
| `children` | array | - | Child nodes |

#### Flipper Container (`type: "flipper"`)

Displays one child at a time, supports animated switching between children. Analogous to Android's ViewFlipper.

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `displayedChild` | number | `0` | Index of the child to display |
| `autoStart` | boolean | `false` | Auto-start flipping animation |
| `flipInterval` | number | `3000` | Interval between flips in ms |
| `inAnimation` | string | - | In animation: `"fade_in"`, `"slide_in_left"` |
| `outAnimation` | string | - | Out animation: `"fade_out"`, `"slide_out_right"` |
| `children` | array | - | Child nodes |

**Transformable properties:**
- `displayedChild` - switch to a specific child by index
- `autoStart` - start/stop auto-flipping
- `flipInterval` - change flip interval

```json
{
  "id": "wizard",
  "type": "flipper",
  "displayedChild": 0,
  "inAnimation": "fade_in",
  "outAnimation": "fade_out",
  "children": [
    {
      "id": "step1",
      "type": "linear",
      "children": [...]
    },
    {
      "id": "step2",
      "type": "linear",
      "children": [...]
    },
    {
      "id": "step3",
      "type": "linear",
      "children": [...]
    }
  ]
}
```

**Switching pages via transform:**

```json
{
  "type": "transform",
  "transform": {
    "type": "property",
    "id": "wizard",
    "property": "displayedChild",
    "value": 1
  }
}
```

---

### Components

#### Hidden (`type: "hidden"`)

Stores data without rendering. Used for form state, IDs, triggers, etc.

| Field | Type | Description |
|-------|------|-------------|
| `value` | any | Stored value |
| `action` | BduiAction | Action to execute when value is transformed (reactive) |

**Basic usage - store data:**

```json
{
  "id": "user_id",
  "type": "hidden",
  "value": "12345"
}
```

**Reactive hidden - trigger action on transform:**

When any component transforms the hidden's value, its action is automatically executed.

```json
{
  "id": "search_trigger",
  "type": "hidden",
  "value": null,
  "action": {
    "type": "rpc",
    "endpoint": "/api/search",
    "method": "POST",
    "payload": {
      "query": { "type": "ref", "id": "search_trigger", "property": "value" }
    }
  }
}
```

**Example: Form validation with reactive hidden:**

```json
[
  {
    "id": "form_valid",
    "type": "hidden",
    "value": false,
    "action": {
      "type": "transform",
      "transform": {
        "type": "property",
        "id": "submit_button",
        "property": "enabled",
        "value": { "type": "ref", "id": "form_valid", "property": "value" }
      }
    }
  },
  {
    "id": "terms_checkbox",
    "type": "checkbox",
    "text": "I agree to terms",
    "action": {
      "type": "transform",
      "transform": {
        "type": "property",
        "id": "form_valid",
        "property": "value",
        "value": { "type": "ref", "id": "terms_checkbox", "property": "checked" }
      }
    }
  },
  {
    "id": "submit_button",
    "type": "button",
    "text": "Submit",
    "enabled": false
  }
]
```

Flow:
1. User checks the checkbox → checkbox's action triggers
2. Transform changes `form_valid.value` to `true`
3. Reactive hidden executes its action
4. Submit button becomes enabled

#### Text (`type: "text"`)

| Field | Type | Description |
|-------|------|-------------|
| `text` | string | Text content |
| `textStyle` | object | Text styling |
| `selectable` | boolean | Allow text selection |
| `autoLink` | boolean | Auto-link URLs, emails |

**TextStyle object:**

| Field | Type | Description |
|-------|------|-------------|
| `textSize` | number | Size in sp |
| `textColor` | string | Color (#RRGGBB) |
| `fontWeight` | string | `"normal"`, `"bold"`, `"medium"`, `"light"` |
| `textAlign` | string | `"start"`, `"center"`, `"end"` |
| `maxLines` | number | Maximum lines |
| `ellipsize` | string | `"start"`, `"middle"`, `"end"`, `"marquee"` |

```json
{
  "id": "title",
  "type": "text",
  "text": "Hello World",
  "textStyle": {
    "textSize": 18,
    "textColor": "#333333",
    "fontWeight": "bold",
    "textAlign": "center"
  }
}
```

#### Button (`type: "button"`)

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `text` | string | - | Button text |
| `variant` | string | `"primary"` | `"primary"`, `"secondary"`, `"outlined"`, `"elevated"`, `"text"` |
| `icon` | string | - | Icon resource name |
| `iconGravity` | string | `"start"` | `"start"`, `"end"`, `"top"`, `"textStart"`, `"textEnd"` |
| `enabled` | boolean | `true` | Enable state |

```json
{
  "id": "submit_btn",
  "type": "button",
  "text": "Submit",
  "variant": "primary",
  "icon": "ic_send",
  "action": { ... }
}
```

#### Icon Button (`type: "icon_button"`)

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `icon` | string | - | Icon resource name |
| `variant` | string | `"standard"` | `"standard"`, `"filled"`, `"tonal"`, `"outlined"` |
| `contentDescription` | string | - | Accessibility description |
| `enabled` | boolean | `true` | Enable state |

#### FAB (`type: "fab"`)

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `icon` | string | - | Icon resource name |
| `text` | string | - | Text for extended FAB |
| `size` | string | `"normal"` | `"mini"`, `"normal"`, `"large"` |
| `extended` | boolean | `false` | Extended FAB mode |

#### Image (`type: "image"`)

| Field | Type | Description |
|-------|------|-------------|
| `src` | string | Image source (see below) |
| `placeholder` | string | Placeholder image resource |
| `imageStyle.scaleType` | string | `"centerCrop"`, `"fitCenter"`, `"centerInside"`, `"fitXY"`, `"center"` |
| `imageStyle.cornerRadius` | number | Corner radius in dp |
| `contentDescription` | string | Accessibility description |

**Supported `src` formats:**

| Format | Example | Description |
|--------|---------|-------------|
| HTTP/HTTPS URL | `"https://example.com/image.png"` | Loads image from network |
| Resource | `"res:ic_placeholder"` | Android drawable resource |
| Resource name | `"ic_placeholder"` | Android drawable resource (without prefix) |
| Inline SVG | `"<svg>...</svg>"` | SVG markup rendered directly |

**Examples:**

```json
{
  "id": "app_icon",
  "type": "image",
  "src": "https://appteka.store/api/1/icon/com.example.app",
  "placeholder": "res:app_placeholder",
  "imageStyle": {
    "scaleType": "centerCrop"
  },
  "layoutParams": {
    "width": "64dp",
    "height": "64dp"
  }
}
```

```json
{
  "id": "category_icon",
  "type": "image",
  "src": "<svg viewBox=\"0 0 24 24\"><path d=\"M12 2L2 7l10 5 10-5-10-5z\"/></svg>",
  "layoutParams": {
    "width": "32dp",
    "height": "32dp"
  }
}
```

#### Icon (`type: "icon"`)

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `icon` | string | - | Icon resource name |
| `size` | number | `24` | Size in dp |
| `tint` | string | - | Tint color |

#### Input (`type: "input"`)

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `text` | string | - | Current text |
| `hint` | string | - | Hint text |
| `helperText` | string | - | Helper text below field |
| `error` | string | - | Error message |
| `variant` | string | `"outlined"` | `"outlined"`, `"filled"` |
| `inputType` | string | `"text"` | `"text"`, `"number"`, `"email"`, `"password"`, `"phone"`, `"multiline"` |
| `maxLines` | number | `1` | Maximum lines |
| `maxLength` | number | - | Character limit |
| `startIcon` | string | - | Leading icon |
| `endIcon` | string | - | Trailing icon |

```json
{
  "id": "email_input",
  "type": "input",
  "hint": "Enter email",
  "inputType": "email",
  "startIcon": "ic_email"
}
```

#### Switch (`type: "switch"`)

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `text` | string | - | Label text |
| `checked` | boolean | `false` | Checked state |
| `enabled` | boolean | `true` | Enable state |

#### Checkbox (`type: "checkbox"`)

Same fields as Switch.

#### Radio Button (`type: "radio"`)

Same fields as Switch.

#### Radio Group (`type: "radio_group"`)

| Field | Type | Description |
|-------|------|-------------|
| `orientation` | string | `"vertical"` or `"horizontal"` |
| `items` | array | `[{ "id": "opt1", "text": "Option 1" }, ...]` |
| `selectedId` | string | ID of selected item |

#### Chip (`type: "chip"`)

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `text` | string | - | Chip text |
| `variant` | string | `"assist"` | `"assist"`, `"filter"`, `"input"`, `"suggestion"` |
| `icon` | string | - | Leading icon |
| `checked` | boolean | `false` | Checked state |
| `checkable` | boolean | `false` | Allow checking |
| `closeIcon` | boolean | `false` | Show close icon |

#### Chip Group (`type: "chip_group"`)

| Field | Type | Description |
|-------|------|-------------|
| `chips` | array | `[{ "id": "c1", "text": "Chip 1", "checked": false }, ...]` |
| `singleSelection` | boolean | Single selection mode |
| `singleLine` | boolean | Single line layout |

#### Progress (`type: "progress"`)

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `variant` | string | `"circular"` | `"circular"`, `"linear"` |
| `indeterminate` | boolean | `true` | Indeterminate mode |
| `progress` | number | `0` | Current progress |
| `max` | number | `100` | Maximum value |

#### Slider (`type: "slider"`)

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `value` | number | `0` | Current value |
| `valueFrom` | number | `0` | Minimum value |
| `valueTo` | number | `100` | Maximum value |
| `stepSize` | number | `0` | Step size |

#### Rating (`type: "rating"`)

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `rating` | number | `0` | Current rating |
| `numStars` | number | `5` | Number of stars |
| `stepSize` | number | `1` | Step size |
| `isIndicator` | boolean | `false` | Read-only mode |

#### Card (`type: "card"`)

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `variant` | string | `"elevated"` | `"elevated"`, `"filled"`, `"outlined"` |
| `cornerRadius` | number | `16` | Corner radius in dp |
| `elevation` | number | `1` | Elevation in dp |
| `children` | array | - | Child nodes |

#### Divider (`type: "divider"`)

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `orientation` | string | `"horizontal"` | `"horizontal"`, `"vertical"` |
| `insetStart` | number | `0` | Start inset in dp |
| `insetEnd` | number | `0` | End inset in dp |
| `thickness` | number | `1` | Thickness in dp |

#### Space (`type: "space"`)

Empty spacer component. Size controlled via `layoutParams`.

#### Toolbar (`type: "toolbar"`)

Material toolbar with navigation, title, subtitle, and menu support.

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `title` | string | - | Toolbar title |
| `subtitle` | string | - | Toolbar subtitle |
| `navigationIcon` | string | - | Navigation icon resource name |
| `navigationAction` | action | - | Action when navigation icon clicked |
| `menu` | array | - | Menu items array |
| `titleCentered` | boolean | `false` | Center the title |
| `subtitleCentered` | boolean | `false` | Center the subtitle |
| `elevation` | number | `0` | Elevation in dp |
| `backgroundColor` | string | - | Background color |
| `titleTextColor` | string | - | Title text color |
| `subtitleTextColor` | string | - | Subtitle text color |
| `navigationIconTint` | string | - | Navigation icon tint color |
| `logo` | string | - | Logo icon resource |
| `contentInsetStart` | number | - | Content inset start in dp |
| `contentInsetEnd` | number | - | Content inset end in dp |

**Menu item object:**

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `id` | string | - | Unique menu item ID |
| `title` | string | - | Menu item title |
| `icon` | string | - | Icon resource name |
| `showAsAction` | string | `"ifRoom"` | `"never"`, `"ifRoom"`, `"always"`, `"withText"`, `"collapseActionView"` |
| `enabled` | boolean | `true` | Enable state |
| `visible` | boolean | `true` | Visibility state |
| `action` | action | - | Action when clicked |
| `iconTint` | string | - | Icon tint color |

```json
{
  "id": "main_toolbar",
  "type": "toolbar",
  "title": "My Screen",
  "subtitle": "Description",
  "navigationIcon": "ic_arrow_back",
  "navigationAction": {
    "type": "callback",
    "name": "back"
  },
  "titleCentered": false,
  "backgroundColor": "#FFFFFF",
  "titleTextColor": "#000000",
  "menu": [
    {
      "id": "menu_search",
      "title": "Search",
      "icon": "ic_search",
      "showAsAction": "always",
      "action": {
        "type": "callback",
        "name": "openSearch"
      }
    },
    {
      "id": "menu_settings",
      "title": "Settings",
      "icon": "ic_settings",
      "showAsAction": "never",
      "action": {
        "type": "callback",
        "name": "openSettings"
      }
    }
  ]
}
```

---

### Layout Parameters

Applied to any node via `layoutParams` field.

| Field | Type | Description |
|-------|------|-------------|
| `width` | string | `"match_parent"`, `"wrap_content"`, or `"100dp"` |
| `height` | string | Same as width |
| `weight` | number | Layout weight (LinearLayout) |
| `gravity` | string | Content gravity |
| `layoutGravity` | string | Gravity in parent |
| `margin` | object | Margin insets |
| `padding` | object | Padding insets |
| `visibility` | string | `"visible"`, `"invisible"`, `"gone"` |
| `alpha` | number | Opacity (0.0 - 1.0) |
| `enabled` | boolean | Enable state |
| `clickable` | boolean | Clickable state |

**Insets object (margin/padding):**

| Field | Type | Description |
|-------|------|-------------|
| `all` | number | All sides |
| `horizontal` | number | Left and right |
| `vertical` | number | Top and bottom |
| `left`/`start` | number | Left/start |
| `right`/`end` | number | Right/end |
| `top` | number | Top |
| `bottom` | number | Bottom |

```json
{
  "layoutParams": {
    "width": "match_parent",
    "height": "wrap_content",
    "margin": { "horizontal": 16, "vertical": 8 },
    "padding": { "all": 12 }
  }
}
```

---

### Actions

Actions define behavior on user interaction. **Every action field expects exactly one action object.** Use `sequence` to combine multiple actions.

#### RPC Action (`type: "rpc"`)

Sends a request to the server.

| Field | Type | Description |
|-------|------|-------------|
| `endpoint` | string | API endpoint |
| `method` | string | HTTP method (`"GET"`, `"POST"`, etc.) |
| `payload` | any | Request payload (supports refs) |

Server response format:
```json
{
  "action": { ... }
}
```

```json
{
  "type": "rpc",
  "endpoint": "/api/submit",
  "method": "POST",
  "payload": {
    "email": { "type": "ref", "id": "email_input", "property": "text" }
  }
}
```

#### Callback Action (`type: "callback"`)

Notifies the host Activity/Fragment.

| Field | Type | Description |
|-------|------|-------------|
| `name` | string | Callback name for routing |
| `data` | any | Callback data (supports refs) |

```json
{
  "type": "callback",
  "name": "openDetails",
  "data": {
    "id": { "type": "ref", "id": "item_id", "property": "value" }
  }
}
```

#### Transform Action (`type: "transform"`)

Applies a UI transform.

| Field | Type | Description |
|-------|------|-------------|
| `transform` | object | Single transform to apply |

```json
{
  "type": "transform",
  "transform": {
    "type": "property",
    "id": "error_text",
    "property": "visibility",
    "value": "visible"
  }
}
```

#### Sequence Action (`type: "sequence"`)

Executes multiple actions sequentially.

| Field | Type | Description |
|-------|------|-------------|
| `actions` | array | Array of actions |

```json
{
  "type": "sequence",
  "actions": [
    {
      "type": "transform",
      "transform": {
        "type": "property",
        "id": "submit_btn",
        "property": "enabled",
        "value": false
      }
    },
    {
      "type": "rpc",
      "endpoint": "/api/submit",
      "method": "POST"
    }
  ]
}
```

#### Route Action (`type: "route"`)

Navigates to a screen within the app. The host Activity handles the actual navigation.

| Field | Type | Description |
|-------|------|-------------|
| `screen` | string | Screen name/identifier |
| `params` | object | Optional parameters for the target screen (supports refs) |

```json
{
  "type": "route",
  "screen": "details",
  "params": {
    "appId": "com.example.app",
    "label": "My Application"
  }
}
```

**With dynamic params using refs:**

```json
{
  "type": "route",
  "screen": "profile",
  "params": {
    "userId": { "type": "ref", "id": "user_id", "property": "value" }
  }
}
```

**Supported Screens:**

The following screens are supported out of the box in `BduiScreenActivity`:

| Screen | Parameters | Description |
|--------|------------|-------------|
| `home` | - | Main home screen |
| `distro` | - | Distribution screen |
| `installed` | `picker`: boolean | Installed apps list |
| `details` | `appId`: string, `packageName`: string, `label`: string (required), `moderation`: boolean, `finishOnly`: boolean | App details page |
| `chat` | `topicId`: int (required), `title`: string | Chat screen |
| `search` | - | Search screen |
| `unpublish` | `appId`: string (required), `label`: string | Unpublish app dialog |
| `unlink` | `appId`: string (required), `label`: string | Unlink app dialog |
| `profile` | `userId`: int (required) | User profile |
| `request_code` | - | Auth request code screen |
| `feed` | `userId`: int (required) | User's feed |
| `agreement` | - | User agreement |
| `post` | - | Create post screen |
| `moderation` | - | Moderation queue |
| `permissions` | `permissions`: string[] (required) | Permissions info |
| `favorite` | `userId`: int (required) | User's favorites |
| `ratings` | `appId`: string (required) | App ratings |
| `uploads` | `userId`: int (required) | User's uploads |
| `downloads` | `userId`: int (required) | User's downloads |
| `subscriptions` | `userId`: int (required), `tab`: string (`"subscribers"` or `"subscriptions"`) | User's subscriptions |
| `reviews` | `userId`: int (required) | User's reviews |
| `about` | - | About screen |
| `settings` | - | Settings screen |
| `bdui` | `url`: string (required), `title`: string | Open another BDUI screen |

**Examples:**

```json
{
  "type": "route",
  "screen": "details",
  "params": {
    "appId": "abc123",
    "label": "My App"
  }
}
```

```json
{
  "type": "route",
  "screen": "profile",
  "params": {
    "userId": 12345
  }
}
```

```json
{
  "type": "route",
  "screen": "bdui",
  "params": {
    "url": "https://api.example.com/bdui/promo.json",
    "title": "Special Offer"
  }
}
```

**Extending routes in subclass:**

If you need to handle additional screens, extend `BduiScreenActivity`:

```kotlin
class MyBduiActivity : BduiScreenActivity() {

    override fun handleRoute(screen: String, params: Map<String, Any>?) {
        when (screen) {
            "custom_screen" -> {
                val customParam = params?.get("customParam") as? String ?: return
                startActivity(CustomActivity.createIntent(this, customParam))
            }
            else -> super.handleRoute(screen, params)
        }
    }
}
```

---

### Transforms

Transforms modify component properties. **Every transform field expects exactly one transform object.** Use `batch` to combine multiple transforms.

#### Property Transform (`type: "property"`)

Changes a single property.

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Component ID |
| `property` | string | Property name |
| `value` | any | New value (supports refs) |

Supported properties:

| Property | Components | Value Type |
|----------|------------|------------|
| `text` | text, input, button | string |
| `visibility` | all | `"visible"`, `"invisible"`, `"gone"` |
| `enabled` | all | boolean |
| `alpha` | all | number (0.0-1.0) |
| `checked` | switch, checkbox, radio, chip | boolean |
| `error` | input | string or null |
| `hint` | input | string |
| `helperText` | input | string |
| `progress` | progress | number |
| `rating` | rating | number |
| `value` | slider, hidden | number or any |

```json
{
  "type": "property",
  "id": "submit_btn",
  "property": "enabled",
  "value": false
}
```

#### Batch Transform (`type: "batch"`)

Applies multiple transforms.

| Field | Type | Description |
|-------|------|-------------|
| `transforms` | array | Array of transforms |

```json
{
  "type": "batch",
  "transforms": [
    { "type": "property", "id": "btn", "property": "enabled", "value": false },
    { "type": "property", "id": "progress", "property": "visibility", "value": "visible" }
  ]
}
```

---

### Refs

Refs provide dynamic data binding. When an action is executed, all refs are resolved to their current values.

| Field | Type | Description |
|-------|------|-------------|
| `type` | string | Always `"ref"` |
| `id` | string | Component ID |
| `property` | string | Property to read |

Available properties for reading:

| Property | Components | Return Type |
|----------|------------|-------------|
| `text` | text, input | string |
| `value` | slider, hidden | number or any |
| `checked` | switch, checkbox, radio, chip | boolean |
| `visibility` | all | string |
| `enabled` | all | boolean |
| `alpha` | all | number |
| `rating` | rating | number |
| `progress` | progress | number |
| `error` | input | string |

```json
{
  "type": "rpc",
  "endpoint": "/api/login",
  "method": "POST",
  "payload": {
    "email": { "type": "ref", "id": "email_input", "property": "text" },
    "password": { "type": "ref", "id": "password_input", "property": "text" },
    "remember": { "type": "ref", "id": "remember_switch", "property": "checked" }
  }
}
```

---

## Examples

### Update Available Screen

A complete example of an "Update Available" screen with Material 3 Expressive styling, Toolbar, and route actions:

```json
{
  "id": "update_screen",
  "type": "linear",
  "orientation": "vertical",
  "layoutParams": {
    "width": "match_parent",
    "height": "match_parent"
  },
  "children": [
    {
      "id": "toolbar",
      "type": "toolbar",
      "title": "Update Available",
      "navigationIcon": "ic_arrow_back",
      "layoutParams": {
        "width": "match_parent",
        "height": "wrap_content"
      },
      "action": {
        "type": "callback",
        "name": "back"
      }
    },
    {
      "id": "content_scroll",
      "type": "scroll",
      "layoutParams": {
        "width": "match_parent",
        "height": "0dp",
        "weight": 1
      },
      "children": [
        {
          "id": "content",
          "type": "linear",
          "orientation": "vertical",
          "layoutParams": {
            "width": "match_parent",
            "height": "wrap_content",
            "padding": { "all": 24 }
          },
          "children": [
            {
              "id": "rocket_icon",
              "type": "image",
              "src": "<svg viewBox='0 0 48 48' fill='none'><defs><linearGradient id='g1' x1='0%' y1='0%' x2='100%' y2='100%'><stop offset='0%' stop-color='#6750A4'/><stop offset='100%' stop-color='#D0BCFF'/></linearGradient></defs><circle cx='24' cy='24' r='22' fill='url(#g1)'/><path d='M24 12c-2 4-3 8-3 12s1 8 3 12c2-4 3-8 3-12s-1-8-3-12z' fill='white'/><path d='M18 24c0-3 2.5-6 6-6s6 3 6 6-2.5 6-6 6-6-3-6-6z' fill='white'/><circle cx='24' cy='24' r='3' fill='#6750A4'/></svg>",
              "layoutParams": {
                "width": "120dp",
                "height": "120dp",
                "gravity": "center_horizontal",
                "margin": { "bottom": 24 }
              }
            },
            {
              "id": "title",
              "type": "text",
              "text": "A New Version is Here!",
              "textStyle": {
                "textSize": 28,
                "fontWeight": "bold",
                "textAlignment": "center"
              },
              "layoutParams": {
                "width": "match_parent",
                "margin": { "bottom": 8 }
              }
            },
            {
              "id": "version_text",
              "type": "text",
              "text": "Version 3.5.0",
              "textStyle": {
                "textSize": 16,
                "textColor": "#6750A4",
                "textAlignment": "center"
              },
              "layoutParams": {
                "width": "match_parent",
                "margin": { "bottom": 24 }
              }
            },
            {
              "id": "description",
              "type": "text",
              "text": "We've been working hard to bring you an amazing update with new features and improvements!",
              "textStyle": {
                "textSize": 16,
                "textAlignment": "center",
                "textColor": "?android:attr/textColorSecondary"
              },
              "layoutParams": {
                "width": "match_parent",
                "margin": { "bottom": 32 }
              }
            },
            {
              "id": "features_card",
              "type": "card",
              "cornerRadius": 16,
              "elevation": 2,
              "layoutParams": {
                "width": "match_parent",
                "margin": { "bottom": 24 }
              },
              "children": [
                {
                  "id": "features_container",
                  "type": "linear",
                  "orientation": "vertical",
                  "layoutParams": {
                    "width": "match_parent",
                    "padding": { "all": 16 }
                  },
                  "children": [
                    {
                      "id": "features_title",
                      "type": "text",
                      "text": "What's New",
                      "textStyle": {
                        "textSize": 18,
                        "fontWeight": "bold"
                      },
                      "layoutParams": { "margin": { "bottom": 16 } }
                    },
                    {
                      "id": "feature_1",
                      "type": "linear",
                      "orientation": "horizontal",
                      "layoutParams": {
                        "width": "match_parent",
                        "margin": { "bottom": 12 }
                      },
                      "children": [
                        {
                          "id": "feature_1_icon",
                          "type": "image",
                          "src": "<svg viewBox='0 0 24 24'><path fill='#6750A4' d='M12 2L4 5v6.09c0 5.05 3.41 9.76 8 10.91 4.59-1.15 8-5.86 8-10.91V5l-8-3zm-1.06 13.54L7.4 12l1.41-1.41 2.12 2.12 4.24-4.24 1.41 1.41-5.64 5.66z'/></svg>",
                          "layoutParams": {
                            "width": "24dp",
                            "height": "24dp",
                            "margin": { "end": 12 }
                          }
                        },
                        {
                          "id": "feature_1_text",
                          "type": "text",
                          "text": "Enhanced security and privacy",
                          "textStyle": { "textSize": 14 },
                          "layoutParams": { "gravity": "center_vertical" }
                        }
                      ]
                    },
                    {
                      "id": "feature_2",
                      "type": "linear",
                      "orientation": "horizontal",
                      "layoutParams": {
                        "width": "match_parent",
                        "margin": { "bottom": 12 }
                      },
                      "children": [
                        {
                          "id": "feature_2_icon",
                          "type": "image",
                          "src": "<svg viewBox='0 0 24 24'><path fill='#6750A4' d='M13 2.05v2.02c3.95.49 7 3.85 7 7.93 0 3.21-1.92 6-4.72 7.28L13 17v5h5l-1.22-1.22C19.91 19.07 22 15.76 22 12c0-5.18-3.95-9.45-9-9.95zM11 2.05C5.94 2.55 2 6.81 2 12c0 3.76 2.09 7.07 5.22 8.78L6 22h5v-5l-2.28 2.28C6.92 18 5 15.21 5 12c0-4.08 3.05-7.44 7-7.93V2.05z'/></svg>",
                          "layoutParams": {
                            "width": "24dp",
                            "height": "24dp",
                            "margin": { "end": 12 }
                          }
                        },
                        {
                          "id": "feature_2_text",
                          "type": "text",
                          "text": "Faster performance",
                          "textStyle": { "textSize": 14 },
                          "layoutParams": { "gravity": "center_vertical" }
                        }
                      ]
                    },
                    {
                      "id": "feature_3",
                      "type": "linear",
                      "orientation": "horizontal",
                      "layoutParams": { "width": "match_parent" },
                      "children": [
                        {
                          "id": "feature_3_icon",
                          "type": "image",
                          "src": "<svg viewBox='0 0 24 24'><path fill='#6750A4' d='M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9c.83 0 1.5-.67 1.5-1.5 0-.39-.15-.74-.39-1.01-.23-.26-.38-.61-.38-.99 0-.83.67-1.5 1.5-1.5H16c2.76 0 5-2.24 5-5 0-4.42-4.03-8-9-8zm-5.5 9c-.83 0-1.5-.67-1.5-1.5S5.67 9 6.5 9 8 9.67 8 10.5 7.33 12 6.5 12zm3-4C8.67 8 8 7.33 8 6.5S8.67 5 9.5 5s1.5.67 1.5 1.5S10.33 8 9.5 8zm5 0c-.83 0-1.5-.67-1.5-1.5S13.67 5 14.5 5s1.5.67 1.5 1.5S15.33 8 14.5 8zm3 4c-.83 0-1.5-.67-1.5-1.5S16.67 9 17.5 9s1.5.67 1.5 1.5-.67 1.5-1.5 1.5z'/></svg>",
                          "layoutParams": {
                            "width": "24dp",
                            "height": "24dp",
                            "margin": { "end": 12 }
                          }
                        },
                        {
                          "id": "feature_3_text",
                          "type": "text",
                          "text": "Fresh new design",
                          "textStyle": { "textSize": 14 },
                          "layoutParams": { "gravity": "center_vertical" }
                        }
                      ]
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "id": "button_container",
      "type": "linear",
      "orientation": "vertical",
      "layoutParams": {
        "width": "match_parent",
        "padding": { "all": 24 }
      },
      "children": [
        {
          "id": "update_button",
          "type": "button",
          "text": "Update Now",
          "variant": "primary",
          "enabled": true,
          "layoutParams": {
            "width": "match_parent",
            "margin": { "bottom": 12 }
          },
          "action": {
            "type": "route",
            "screen": "details",
            "params": {
              "appId": "self",
              "label": "Appteka"
            }
          }
        },
        {
          "id": "later_button",
          "type": "button",
          "text": "Maybe Later",
          "variant": "text",
          "enabled": true,
          "layoutParams": { "width": "match_parent" },
          "action": {
            "type": "callback",
            "name": "close"
          }
        }
      ]
    }
  ]
}
```

### Login Form

```json
{
  "id": "login_form",
  "type": "linear",
  "orientation": "vertical",
  "layoutParams": {
    "width": "match_parent",
    "height": "wrap_content",
    "padding": { "all": 16 }
  },
  "children": [
    {
      "id": "title",
      "type": "text",
      "text": "Login",
      "textStyle": { "textSize": 24, "fontWeight": "bold" },
      "layoutParams": { "margin": { "bottom": 24 } }
    },
    {
      "id": "email_input",
      "type": "input",
      "hint": "Email",
      "inputType": "email",
      "startIcon": "ic_email",
      "layoutParams": { 
        "width": "match_parent",
        "margin": { "bottom": 16 } 
      }
    },
    {
      "id": "password_input",
      "type": "input",
      "hint": "Password",
      "inputType": "password",
      "startIcon": "ic_lock",
      "layoutParams": { 
        "width": "match_parent",
        "margin": { "bottom": 16 } 
      }
    },
    {
      "id": "remember_switch",
      "type": "switch",
      "text": "Remember me",
      "checked": false,
      "layoutParams": { "margin": { "bottom": 24 } }
    },
    {
      "id": "error_text",
      "type": "text",
      "text": "",
      "textStyle": { "textColor": "#D32F2F" },
      "layoutParams": { 
        "visibility": "gone",
        "margin": { "bottom": 16 } 
      }
    },
    {
      "id": "submit_btn",
      "type": "button",
      "text": "Login",
      "variant": "primary",
      "layoutParams": { "width": "match_parent" },
      "action": {
        "type": "sequence",
        "actions": [
          {
            "type": "transform",
            "transform": {
              "type": "batch",
              "transforms": [
                { "type": "property", "id": "submit_btn", "property": "enabled", "value": false },
                { "type": "property", "id": "error_text", "property": "visibility", "value": "gone" }
              ]
            }
          },
          {
            "type": "rpc",
            "endpoint": "/api/auth/login",
            "method": "POST",
            "payload": {
              "email": { "type": "ref", "id": "email_input", "property": "text" },
              "password": { "type": "ref", "id": "password_input", "property": "text" },
              "remember": { "type": "ref", "id": "remember_switch", "property": "checked" }
            }
          }
        ]
      }
    }
  ]
}
```

### Server Response - Success

```json
{
  "action": {
    "type": "callback",
    "name": "loginSuccess",
    "data": { "userId": "123", "token": "abc..." }
  }
}
```

### Server Response - Error

```json
{
  "action": {
    "type": "sequence",
    "actions": [
      {
        "type": "transform",
        "transform": {
          "type": "batch",
          "transforms": [
            { "type": "property", "id": "submit_btn", "property": "enabled", "value": true },
            { "type": "property", "id": "error_text", "property": "text", "value": "Invalid credentials" },
            { "type": "property", "id": "error_text", "property": "visibility", "value": "visible" }
          ]
        }
      }
    ]
  }
}
```

---

## File Structure

```
util/bdui/
├── BduiView.kt                    # Main view component
├── BduiRenderer.kt                # Rendering orchestrator
├── BduiActionHandler.kt           # Action execution
├── BduiTransformHandler.kt        # Transform application
├── BduiRefResolver.kt             # Ref resolution
├── README.md                      # This documentation
│
├── model/
│   ├── BduiNode.kt                # Base node + BduiRef
│   ├── BduiLayoutParams.kt        # Layout parameters
│   ├── BduiStyle.kt               # Style definitions
│   │
│   ├── action/
│   │   └── BduiAction.kt          # Action models
│   │
│   ├── transform/
│   │   └── BduiTransform.kt       # Transform models
│   │
│   ├── container/
│   │   └── BduiContainer.kt       # Container models
│   │
│   └── component/
│       └── BduiComponent.kt       # Component models
│
├── factory/
│   ├── BduiComponentFactory.kt    # Component creation
│   ├── BduiContainerFactory.kt    # Container creation
│   └── BduiLayoutParamsFactory.kt # Layout params creation
│
└── parser/
    └── BduiJsonParser.kt          # JSON parsing with Gson
```

