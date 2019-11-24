package nerd.cave.web.wx

import io.vertx.core.json.JsonObject
import nerd.cave.web.client.WebClient

class WXWebClient(val webClient: WebClient) {
    private val appId = retrieveKey(APP_ID_KEY)
    private val appSecret = retrieveKey(APP_SECRET_KEY)
    private val host = "https://api.weixin.qq.com"

    private val baseQuery = mapOf(
        "appid" to appId,
        "secret" to appSecret
    )

    companion object {
        private val APP_ID_KEY = "WECHAT_APP_SECRET"
        private val APP_SECRET_KEY = "WECHAT_APP_SECRET"
        private val CODE_2_SESSION: String = "/sns/jscode2session"

        fun retrieveKey(key:String): String {
            return System.getenv(key)
        }
    }


    suspend fun code2Session(code:String): JsonObject {
        val fullPath = createFulPath(CODE_2_SESSION, "js_code" to code, "grand_type" to "authorization_code")
        println("code2Session path: $fullPath")
        return webClient.get(fullPath)
    }

    private fun createFulPath(path:String, vararg queryStringPairs: Pair<String, String>): String {
        val queryString = baseQuery.plus(queryStringPairs.toList())
            .map { "${it.key}=${it.value}" }
            .reduce { acc, s -> "$acc&$s" }
        return "$host$path?$queryString"
    }
}