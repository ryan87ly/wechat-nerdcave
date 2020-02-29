package nerd.cave.web.wx.payment

import nerd.cave.web.client.WebClient
import nerd.cave.web.wx.WXApi
import nerd.cave.web.wx.config.WXConfig
import nerd.cave.xml.fromXmlToMap
import org.slf4j.LoggerFactory
import kotlin.math.abs
import kotlin.random.Random

class WXPayClient(private val webClient: WebClient, private val wxConfig: WXConfig, private val paymentSecretRetriever: PaymentSecretRetriever) {
    companion object {
        private val logger = LoggerFactory.getLogger(WXPayClient::class.java)
    }

    val appId = wxConfig.appId

    private val baseParams = listOf(
        "appid" to wxConfig.appId,
        "mch_id" to wxConfig.mchId
    )

    suspend fun placeUnifiedOrder(openid:String, paymentId: String, productBody:String, fee:Int, creatorIP:String): Map<String, String> {
        val variablesToBeSigned = baseParams.plus(listOf(
            "nonce_str" to createNonseStr(),
            "body" to productBody,
            "out_trade_no" to paymentId,
            "total_fee" to fee,
            "spbill_create_ip" to  creatorIP,
            "openid" to openid,
            "notify_url" to wxConfig.notifyUrl,
            "trade_type" to "JSAPI"
        ))
        val secret = paymentSecretRetriever.retrievePaymentSecret()
        val signature = nerd.cave.web.wx.sign(secret, *variablesToBeSigned.toTypedArray())
        val variables = variablesToBeSigned.plus("sign" to signature)
        logger.info("variables ${variables.toMap()}")
        val r = webClient.postXml(createRequestUri(WXApi.UNIFIED_ORDER), variables.toMap())
        logger.info("result $r")
        return r
    }

    suspend fun sign(vararg params: Pair<String, Any>): String {
        val secret = paymentSecretRetriever.retrievePaymentSecret()
        return nerd.cave.web.wx.sign(secret, *params)
    }

    fun createNonseStr(): String {
        return abs(Random.nextInt()).toString()
    }

    private fun createRequestUri(wxApi: WXApi): String {
        return "${wxConfig.paymentHost}${wxApi.path}"
    }
}