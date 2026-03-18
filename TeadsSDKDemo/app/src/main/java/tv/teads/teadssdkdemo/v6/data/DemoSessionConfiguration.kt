package tv.teads.teadssdkdemo.v6.data

import tv.teads.teadssdkdemo.v6.domain.IntegrationType

/**
 * Configuration object that holds default values and current session values
 */
object DemoSessionConfiguration {

    private var currentPlacementId: String = ""
    private var currentArticleUrl: String = ""
    private var currentIntegration: IntegrationType? = null

    val DEFAULT_INTEGRATION = IntegrationType.WEBVIEW_XML
    const val DEFAULT_MEDIA_PID = "84242" // Landscape
    const val DEFAULT_ARTICLE_URL = "https://mobile-demo.outbrain.com/"

    fun getIntegrationOrDefault(): IntegrationType {
        return currentIntegration ?: DEFAULT_INTEGRATION
    }

    fun getPlacementIdOrDefault(): String {
        if (currentPlacementId.isNotBlank()) return currentPlacementId
        return DEFAULT_MEDIA_PID
    }

    fun getArticleUrlOrDefault(): String {
        return currentArticleUrl.ifBlank { DEFAULT_ARTICLE_URL }
    }

    fun setPlacementId(placementId: String) {
        currentPlacementId = placementId
    }

    fun setArticleUrl(articleUrl: String) {
        currentArticleUrl = articleUrl
    }

    fun setIntegration(integration: IntegrationType?) {
        currentIntegration = integration
    }
}
