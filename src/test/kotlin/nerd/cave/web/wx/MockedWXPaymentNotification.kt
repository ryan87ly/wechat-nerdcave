package nerd.cave.web.wx

import kotlinx.coroutines.runBlocking
import nerd.cave.Environment
import nerd.cave.web.client.WebClient
import nerd.cave.web.wx.config.WXConfig
import org.junit.Ignore
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

@Ignore
internal class MockedWXPaymentNotification {

    @Ignore
    fun `Mock success payment notification`() = runBlocking {
        val clock = Clock.system(ZoneOffset.ofHours(8))
        val env = Environment.PROD
        val config = WXConfig.forEnv(env)

        val tradeId = "5e354afebf83aa6cdacf0f79"
        val totalFee = 201
        val transactionId = ZonedDateTime.now(clock).toEpochSecond().toString()
        val timeEnd = ZonedDateTime.now(clock).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

        val openid = "o7tbT5GlL8DhAOifK0DmBXkDto-s"
        val resultCode = "SUCCESS"
        val returnCode = "SUCCESS"

        val response = arrayOf<Pair<String, Any>>(
            "bank_type" to "OTHERS",
            "cash_fee" to totalFee.toString(),
            "mch_id" to config.mchId,
            "openid" to openid,
            "out_trade_no" to tradeId,
            "result_code" to resultCode,
            "return_code" to returnCode,
            "time_end" to timeEnd,
            "total_fee" to totalFee.toString(),
            "trade_type" to "JSAPI",
            "transaction_id" to transactionId
        )
        val signature = sign(config.paymentSecret, *response)
        val signedResponse = (response + ("sign" to signature)).toMap()
        print("signed response $signedResponse")

        val webClient = WebClient(Executors.newSingleThreadExecutor())
        webClient.start()
        val reply = webClient.postXml("http://localhost:8080/api/notification/payment", signedResponse)
        print("reply $reply")
    }

    @Ignore
    fun `Payment Id not found`() = runBlocking {
        val clock = Clock.system(ZoneOffset.ofHours(8))
        val env = Environment.PROD
        val config = WXConfig.forEnv(env)

        val tradeId = "5e354afebf83aa6cdacf0f78"
        val totalFee = 201
        val transactionId = ZonedDateTime.now(clock).toEpochSecond().toString()
        val timeEnd = ZonedDateTime.now(clock).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

        val openid = "o7tbT5GlL8DhAOifK0DmBXkDto-s"
        val resultCode = "SUCCESS"
        val returnCode = "SUCCESS"

        val response = arrayOf<Pair<String, Any>>(
            "bank_type" to "OTHERS",
            "cash_fee" to totalFee.toString(),
            "mch_id" to config.mchId,
            "openid" to openid,
            "out_trade_no" to tradeId,
            "result_code" to resultCode,
            "return_code" to returnCode,
            "time_end" to timeEnd,
            "total_fee" to totalFee.toString(),
            "trade_type" to "JSAPI",
            "transaction_id" to transactionId
        )
        val signature = sign(config.paymentSecret, *response)
        val signedResponse = (response + ("sign" to signature)).toMap()
        print("signed response $signedResponse")

        val webClient = WebClient(Executors.newSingleThreadExecutor())
        webClient.start()
        val reply = webClient.postXml("http://localhost:8080/api/notification/payment", signedResponse)
        print("reply $reply")
    }
}