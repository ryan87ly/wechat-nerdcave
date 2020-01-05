package nerd.cave.web.wx.payment

import nerd.cave.Environment
import nerd.cave.web.client.WebClient
import nerd.cave.web.exceptions.BadRequestException
import nerd.cave.web.wx.config.WXConfig
import nerd.cave.web.wx.sign
import org.slf4j.LoggerFactory
import java.util.*

interface PaymentSecretRetriever {
    companion object {
        fun forEnv(env: Environment, wxConfig: WXConfig, webClient: WebClient): PaymentSecretRetriever {
            return when(env) {
                Environment.UAT -> SandboxPaymentSecretRetriever(wxConfig, webClient)
                Environment.LOCAL -> SandboxPaymentSecretRetriever(wxConfig, webClient)
                Environment.PROD -> ProdPaymentSecretRetriever(wxConfig)
            }
        }
    }

    suspend fun retrievePaymentSecret(): String
    suspend fun refreshAndRetrievePaymentSecret(): String
}

class SandboxPaymentSecretRetriever(private val wxConfig: WXConfig, private val webClient: WebClient) : PaymentSecretRetriever {
    companion object {
        private const val GET_SIGN_KEY_URL = "https://api.mch.weixin.qq.com/sandboxnew/pay/getsignkey"
        private const val SANDBOX_SIGNKEY = "sandbox_signkey"
        private val logger = LoggerFactory.getLogger(SandboxPaymentSecretRetriever::class.java)
    }

    private var paymentSecret: String? = null

    override suspend fun retrievePaymentSecret(): String {
        return paymentSecret ?: refreshAndRetrievePaymentSecret()
    }

    override suspend fun refreshAndRetrievePaymentSecret(): String {
        val randStr = UUID.randomUUID().toString()
        val variablesToBeSigned = listOf(
            "mch_id" to wxConfig.mchId,
            "nonce_str" to randStr
        )
        val signature = sign(wxConfig.paymentSecret, *variablesToBeSigned.toTypedArray())
        val variables = variablesToBeSigned.plus("sign" to signature)
        val response = webClient.postXml(GET_SIGN_KEY_URL, variables.toMap()).toWXPaymentResponse()
        if (response.isSuccess) {
            paymentSecret = response[SANDBOX_SIGNKEY]
            return paymentSecret!!
        } else {
            logger.error("Unable to refresh sandbox payment secret, response: [$response]")
            throw BadRequestException("Unable to refresh sandbox payment secret, response: [$response]")
        }
    }
}

class ProdPaymentSecretRetriever(private val wxConfig: WXConfig): PaymentSecretRetriever {
    override suspend fun retrievePaymentSecret(): String {
        return wxConfig.paymentSecret
    }

    override suspend fun refreshAndRetrievePaymentSecret(): String {
        return wxConfig.paymentSecret
    }

}