package tv.teads.teadssdkdemo.v6.ui.base.navigation

import androidx.fragment.app.Fragment
import tv.teads.teadssdkdemo.v5.InReadWebViewFragment
import tv.teads.teadssdkdemo.v6.domain.DisplayMode
import tv.teads.teadssdkdemo.v6.domain.FormatType
import tv.teads.teadssdkdemo.v6.domain.IntegrationType
import tv.teads.teadssdkdemo.v6.domain.ProviderType

/**
 * Sealed class representing all possible navigation routes
 */
sealed class Route {
    data object Demo : Route()
    data object InReadWebView : Route()
    data object InReadWebViewColumn : Route()
}

/**
 * Route factory that creates routes based on configuration
 */
object RouteFactory {

    /**
     * Create route based on format, provider and integration configuration
     */
    fun createRoute(
        format: FormatType,
        provider: ProviderType,
        integration: IntegrationType,
        displayMode: DisplayMode?
    ): Route {
        return when (integration) {
            IntegrationType.WEBVIEW_XML -> Route.InReadWebView
            IntegrationType.WEBVIEW_COMPOSE -> Route.InReadWebViewColumn
        }
    }
}

/**
 * Extension functions for Route utilities
 */
fun Route.getFragmentClass(): Class<out Fragment> {
    return when (this) {
        Route.InReadWebView -> InReadWebViewFragment::class.java
        else -> throw IllegalArgumentException("No fragment defined for route: $this")
    }
}

fun Route.getFragmentTag(): String = this.getTitle().filter { !it.isWhitespace() }

fun String.getRouteFromTag(): Route {
    return when (this) {
        "InReadWebViewFragment" -> Route.InReadWebView
        "InReadWebViewColumn" -> Route.InReadWebViewColumn
        else -> throw IllegalArgumentException("No fragment found for tag: $this")
    }
}

fun Route.getTitle(): String {
    return when (this) {
        Route.Demo -> "Teads SDK Demo"
        Route.InReadWebView -> "InRead WebView Fragment"
        Route.InReadWebViewColumn -> "InRead WebView Column"
    }
}
