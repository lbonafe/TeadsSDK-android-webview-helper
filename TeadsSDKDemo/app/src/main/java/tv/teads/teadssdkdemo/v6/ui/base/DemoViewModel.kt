package tv.teads.teadssdkdemo.v6.ui.base

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tv.teads.teadssdkdemo.v6.data.DemoSessionConfiguration
import tv.teads.teadssdkdemo.v6.domain.IntegrationType
import tv.teads.teadssdkdemo.v6.ui.base.components.ChipData
import tv.teads.teadssdkdemo.v6.ui.base.navigation.Route
import tv.teads.teadssdkdemo.v6.ui.base.navigation.RouteFactory
import tv.teads.teadssdkdemo.v6.domain.DisplayMode
import tv.teads.teadssdkdemo.v6.domain.FormatType
import tv.teads.teadssdkdemo.v6.domain.ProviderType

class DemoViewModel : ViewModel() {

    // Navigation callback
    private var onNavigateCallback: ((Route) -> Unit)? = null

    // Scroll state persistence for DemoScreen
    private var _scrollState: ScrollState? = null
    val scrollState: ScrollState
        get() = _scrollState ?: ScrollState(0).also { _scrollState = it }

    val selectedFormat: FormatType = FormatType.MEDIA

    private var selectedIntegration: IntegrationType? by mutableStateOf(null)

    // Text fields for placement configuration
    private val _placementId = MutableStateFlow("")
    val placementId: StateFlow<String> = _placementId.asStateFlow()

    private val _articleUrl = MutableStateFlow("")
    val articleUrl: StateFlow<String> = _articleUrl.asStateFlow()

    // PID presets for Media format
    private val mediaPids = listOf(
        "Landscape" to "84242",
        "Vertical" to "127546",
        "Square" to "127547",
        "Carousel" to "128779"
    )

    private val integrationTypes = listOf(
        IntegrationType.WEBVIEW_XML,
        IntegrationType.WEBVIEW_COMPOSE
    )

    init {
        selectedIntegration = DemoSessionConfiguration.getIntegrationOrDefault()
        _placementId.value = DemoSessionConfiguration.getPlacementIdOrDefault()
        _articleUrl.value = DemoSessionConfiguration.getArticleUrlOrDefault()
    }

    /**
     * Set navigation callback
     */
    fun setOnNavigateCallback(callback: (Route) -> Unit) {
        onNavigateCallback = callback
    }

    /**
     * Trigger navigation based on current configuration
     */
    fun launchNavigation() {
        val route = RouteFactory.createRoute(
            format = FormatType.MEDIA,
            provider = ProviderType.DIRECT,
            integration = DemoSessionConfiguration.getIntegrationOrDefault(),
            displayMode = DisplayMode.MEDIA_ONLY
        )
        onNavigateCallback?.invoke(route)
    }

    fun updatePlacementId(pid: String) {
        _placementId.value = pid
        DemoSessionConfiguration.setPlacementId(pid)
    }

    private fun updateArticleUrl(articleUrl: String) {
        _articleUrl.value = articleUrl
        DemoSessionConfiguration.setArticleUrl(articleUrl)
    }

    private fun updateIntegration(integration: IntegrationType) {
        selectedIntegration = integration
        DemoSessionConfiguration.setIntegration(integration)
    }

    fun getFormatChips(): List<ChipData> = listOf(
        ChipData(
            id = 0,
            text = FormatType.MEDIA.displayName,
            isSelected = true
        )
    )

    fun getInputMethod(): KeyboardType = KeyboardType.Number

    fun getPidChips(): List<ChipData> = mediaPids.mapIndexed { index, (label, _) ->
        ChipData(
            id = index,
            text = label,
            isSelected = _placementId.value == mediaPids[index].second
        )
    }

    fun getIntegrationChips(): List<ChipData> {
        return integrationTypes.mapIndexed { index, integration ->
            ChipData(
                id = index,
                text = integration.displayName,
                isSelected = if (integrationTypes.contains(selectedIntegration)) {
                    integration == selectedIntegration
                } else {
                    updateIntegration(integration)
                    index == 0
                }
            )
        }
    }

    fun onPidChipClick(index: Int) {
        if (index in mediaPids.indices) {
            val pid = mediaPids[index].second
            updatePlacementId(pid)
        }
    }

    fun onArticleUrlChange(articleUrl: String) {
        updateArticleUrl(articleUrl)
    }

    fun onIntegrationChipClick(index: Int) {
        if (index in integrationTypes.indices) {
            val integration = integrationTypes[index]
            updateIntegration(integration)
        }
    }
}
