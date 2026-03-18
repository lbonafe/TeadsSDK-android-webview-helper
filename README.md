# TeadsSDK Android — WebView Helper Integration

> This repository is a **fork** of [teads/TeadsSDK-android](https://github.com/teads/TeadsSDK-android).
>
> It exists to demonstrate how to display **Teads inRead ads inside WebView content** using the `webviewhelper` module — a use case not covered by the standard SDK integration guides.

## The Problem

When your app renders editorial content inside a `WebView`, placing a Teads inRead ad inline with that content is not straightforward. The ad is a native Android view, but the content lives in a web page — scroll positions, layout dimensions, and coordinate systems don't align by default.

## What the WebView Helper Does

The `webviewhelper` module bridges native Android and the WebView's JavaScript layer to:

1. **Inject a JavaScript bootstrap** into the loaded web page
2. **Find a placeholder element** in the HTML (e.g. `<div id="teads-placement-slot">`)
3. **Track the placeholder's position** using a marker-based calculation that is completely scroll-independent
4. **Synchronize a native ad overlay** that follows the placeholder as the user scrolls, creating the illusion that the ad is part of the web content

## Architecture

| Class | Role |
|---|---|
| `SyncAdWebView` | Main orchestrator — creates the container hierarchy, listens to scroll events, and positions the native ad overlay to match the JS placeholder |
| `WebViewHelper` | JavaScript bridge — injects `bootstrap.js`, sends commands (insert/update/open/close slot), and receives position callbacks via `@JavascriptInterface` |
| `ObservableWebView` | Custom `WebView` subclass that dispatches scroll events to a listener on every frame |
| `ObservableContainerAdView` | Transparent overlay that holds the ad view and forwards touch events back to the `WebView` so users can still interact with content |
| `bootstrap.js` | Injected JavaScript — creates the placeholder, tracks its document-absolute position via a hidden marker, and notifies native on layout changes |

## Integration Guide

### 1. Copy the module into your project

The `webviewhelper` module is **not published to any artifact repository**. You need to copy it directly into your project:

1. Copy the `webviewhelper/` directory from this repository into your project root (next to your `app/` module)
2. Register it in your `settings.gradle.kts`:
   ```kotlin
   include(":webviewhelper")
   ```
3. Add the Teads SDK dependency and the module in your `app/build.gradle.kts`:
   ```kotlin
   dependencies {
       implementation("tv.teads.sdk.android:sdk:<version>") {
           isTransitive = true
       }
       implementation(projects.webviewhelper)
   }
   ```

### 2. Add a placement slot in your HTML

Insert an empty `<div>` where you want the ad to appear in your web content:

```html
<body>
    <p>Article content above the ad...</p>

    <div id="teads-placement-slot"></div>

    <p>Article content below the ad...</p>
</body>
```

### 3. Create a custom WebViewClient

The JavaScript bridge must be injected **after** the page finishes loading:

```kotlin
class CustomInReadWebviewClient(
    private val syncAdWebView: SyncAdWebView
) : WebViewClient() {

    override fun onPageFinished(view: WebView, url: String) {
        syncAdWebView.injectJS()
        super.onPageFinished(view, url)
    }
}
```

### 4. Set up the Fragment (or Compose screen)

```kotlin
class InReadWebViewFragment : Fragment(), SyncAdWebView.Listener {

    private lateinit var syncAdWebView: SyncAdWebView
    private lateinit var adPlacement: InReadAdPlacement
    private lateinit var webView: ObservableWebView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById(R.id.webview)

        // 1. Create the ad placement
        val placementSettings = AdPlacementSettings.Builder()
            .enableDebug()
            .build()

        adPlacement = TeadsSDK.createInReadPlacement(
            requireActivity(),
            YOUR_PLACEMENT_ID,
            placementSettings
        )

        // 2. Initialize the WebView helper with a CSS selector
        syncAdWebView = SyncAdWebView(
            requireContext(),
            webView,
            this@InReadWebViewFragment,
            "#teads-placement-slot"
        )

        // 3. Configure and load the WebView
        with(webView) {
            settings.javaScriptEnabled = true
            overScrollMode = View.OVER_SCROLL_NEVER
            webViewClient = CustomInReadWebviewClient(syncAdWebView)
            loadUrl("file:///android_asset/your_article.html")
        }
    }

    // 4. Called when the JS bridge is ready — request the ad
    override fun onHelperReady(adContainer: ViewGroup) {
        val requestSettings = AdRequestSettings.Builder()
            .pageSlotUrl("https://your-article-url.com")
            .build()

        adPlacement.requestAd(requestSettings, object : InReadAdViewListener {
            override fun adOpportunityTrackerView(trackerView: AdOpportunityTrackerView) {
                syncAdWebView.registerTrackerView(trackerView)
            }

            override fun onAdReceived(ad: InReadAdView, adRatio: AdRatio) {
                syncAdWebView.registerAdView(ad)
                syncAdWebView.setAdRatio(adRatio)
            }

            override fun onAdRatioUpdate(adRatio: AdRatio) {
                syncAdWebView.setAdRatio(adRatio)
            }

            override fun onAdClosed() {
                syncAdWebView.closeAd()
            }

            override fun onAdError(code: Int, description: String) {
                syncAdWebView.clean()
            }

            override fun onFailToReceiveAd(failReason: String) {
                syncAdWebView.clean()
            }

            override fun onAdClicked() {}
            override fun onAdImpression() {}
        })
    }

    // 5. Handle orientation changes
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        syncAdWebView.onConfigurationChanged()
    }

    // 6. Clean up
    override fun onDestroy() {
        super.onDestroy()
        syncAdWebView.clean()
    }
}
```

### 5. Layout

Your layout only needs an `ObservableWebView` — the helper will wrap it in a `FrameLayout` container at runtime:

```xml
<RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <tv.teads.webviewhelper.baseView.ObservableWebView
        android:id="@+id/webview"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</RelativeLayout>
```

## Running the Demo App

1. Clone this repository
2. Open `TeadsSDKDemo/` in Android Studio
3. Build and run on a device or emulator (API 21+)
4. Select **WebView XML** or **WebView Compose** integration and tap **Launch Article**

The demo app includes both an XML Fragment implementation and a Jetpack Compose implementation. See:
- [`InReadWebViewFragment.kt`](TeadsSDKDemo/app/src/main/java/tv/teads/teadssdkdemo/v5/InReadWebViewFragment.kt) — XML/Fragment example
- [`InReadWebViewColumnScreen.kt`](TeadsSDKDemo/app/src/main/java/tv/teads/teadssdkdemo/v5/InReadWebViewColumnScreen.kt) — Compose example

## Requirements

- Android SDK 21+
- Java 17
- AndroidX ([Migrate to AndroidX](https://developer.android.com/jetpack/androidx/migrate))
- Teads SDK 6.x

## Upstream SDK Documentation

For general Teads SDK integration (non-WebView), see:
- [Teads SDK Documentation](https://developers.teads.com/docs/Android-SDK/Getting-Started/)
- [Upstream repository](https://github.com/teads/TeadsSDK-android)
