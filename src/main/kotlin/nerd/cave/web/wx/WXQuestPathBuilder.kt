package nerd.cave.web.wx

import nerd.cave.web.wx.config.WXConfig

class WXQuestPathBuilder(val wxConfig: WXConfig) {
    private val baseQuery = mapOf(
        "appid" to wxConfig.appId,
        "secret" to wxConfig.appSecret
    )

    fun createApiRequestPath(wxApi: WXApi, vararg queryStringPairs: Pair<String, String>): String {
        return createApiRequestPath(wxApi, wxHost = wxConfig.apiHost, queryStringPairs = *queryStringPairs)
    }

    fun createPaymentApiRequestBody(wxApi: WXApi, vararg queryStringPairs: Pair<String, String>): String {
        return createApiRequestPath(wxApi, wxHost = wxConfig.paymentHost, queryStringPairs = *queryStringPairs)
    }

    private fun createApiRequestPath(wxApi: WXApi, wxHost: String, vararg queryStringPairs: Pair<String, String>): String {
        val queryString = baseQuery.plus(queryStringPairs.toList())
            .map { "${it.key}=${it.value}" }
            .reduce { acc, s -> "$acc&$s" }
        return "${wxHost}${wxApi.path}?$queryString"
    }
}

enum class WXApi(val path: String) {
    CODE_2_SESSION("/sns/jscode2session"),
    UNIFIED_ORDER("/unifiedorder")
}