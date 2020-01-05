package nerd.cave.web.wx

import io.vertx.core.json.JsonObject
import nerd.cave.web.client.WebClient
import nerd.cave.web.wx.config.WXConfig

class WXWebClient(val webClient: WebClient, wxConfig: WXConfig) {
    private val wxQuestPathBuilder = WXQuestPathBuilder(wxConfig)

    suspend fun code2Session(code:String): JsonObject {
        val requestPath = wxQuestPathBuilder.createApiRequestPath(WXApi.CODE_2_SESSION, "js_code" to code, "grand_type" to "authorization_code")
        println("code2Session path: $requestPath")
        return webClient.getJson(requestPath)
    }
}