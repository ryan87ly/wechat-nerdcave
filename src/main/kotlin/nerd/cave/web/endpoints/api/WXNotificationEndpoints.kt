package nerd.cave.web.endpoints.api

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.model.api.order.wechat.WXPaymentCallback
import nerd.cave.service.order.OrderService
import nerd.cave.service.order.hasProceeded
import nerd.cave.store.WXPaymentCallbackStoreService
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.extentions.coroutine
import nerd.cave.web.extentions.endIfOpen
import nerd.cave.web.wx.payment.*
import nerd.cave.xml.fromXmlToMap
import nerd.cave.xml.toXmlString
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.ZonedDateTime

class WXNotificationEndpoints(
    vertx: Vertx,
    private val clock: Clock,
    private val paymentSecretRetriever: PaymentSecretRetriever,
    private val wxPaymentCallbackStoreService: WXPaymentCallbackStoreService,
    private val orderService: OrderService
): HttpEndpoint {
    companion object {
        private val logger = LoggerFactory.getLogger(WXNotificationEndpoints::class.java)
    }

    override val router = Router.router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        post("/payment") { paymentCallback(it) }
    }

    private suspend fun paymentCallback(ctx: RoutingContext) {
        logger.info("Payment callback involved, body ${ctx.bodyAsString}")
        val paymentResult: WXPayResponse
        try {
           paymentResult = ctx.bodyAsString.fromXmlToMap().toWXPaymentResponse()
        } catch (e: Exception) {
            logger.error("Unable to parse paymentcallbak message, message：[${ctx.bodyAsString}]", e)
            ctx.response().respondFailed("Invalid content format")
            return
        }
        val timestamp = ZonedDateTime.now(clock)
        wxPaymentCallbackStoreService.insertNotification(WXPaymentCallback(timestamp, ctx.bodyAsString, paymentResult))
        if (!paymentResult.verifyContent(paymentSecretRetriever.retrievePaymentSecret())){
            logger.error("Unable to verify payment callback's signature [${ctx.bodyAsString}], please check")
            ctx.response().respondFailed("Invalid signature")
            return
        }
        if (!paymentResult.isResponseSuccess) {
            logger.info("Payment response failed")
            ctx.response().respondSuccess()
            return
        }
        if (!paymentResult.isPaymentSuccess) {
            logger.info("Payment failed")
            ctx.response().respondSuccess()
            return
        }
        val paymentId = paymentResult[WXPayApiFields.OUT_TRADE_NO]
        val transactionId = paymentResult[WXPayApiFields.TRANSACTION_ID]
        val amount = paymentResult[WXPayApiFields.TOTAL_FEE]?.toInt()
        val transactionTime = paymentResult[WXPayApiFields.TIME_END]
        if (paymentId == null || transactionId == null || amount == null || transactionTime == null) {
            ctx.response().respondFailed("Mandatory parameters not found")
            return
        }
        val payment = orderService.fetchPayment(paymentId)
        if (payment == null) {
            logger.warn("Unable to find payment, id:[${paymentId}]")
            ctx.response().respondFailed("Unable to find NerdCave payment")
            return
        }
        if (payment.hasProceeded()) {
            logger.warn("Payment[${paymentId}] has proceeded, skipping...")
            ctx.response().respondSuccess()
            return
        }
        if (payment.totalFee != amount) {
            logger.warn("Payment amount not equal, id:[${paymentId}], expected:[${payment.totalFee}], notified:[${amount}]")
            ctx.response().respondFailed("Payment amount not equal")
            return
        }
        try {
            orderService.redeemPayment(payment, transactionId, paymentResult)
        } catch (e: Exception) {
            logger.error("Unable to redeem payment, id:[${paymentId}]", e)
            ctx.response().respondFailed("Unable to redeem payment")
        }
        ctx.response().respondSuccess()
    }

    private fun HttpServerResponse.respondFailed(msg: String) {
        endIfOpen(
            HttpResponseStatus.OK,
            mapOf(
                WXPayApiFields.RETURN_CODE to "FAIL",
                WXPayApiFields.RETURN_MSG to msg
            ).toXmlString()
        )
    }

    private fun HttpServerResponse.respondSuccess() {
        endIfOpen(
            HttpResponseStatus.OK,
            mapOf(
                WXPayApiFields.RETURN_CODE to "SUCCESS",
                WXPayApiFields.RETURN_MSG to "OK"
            ).toXmlString()
        )
    }
}