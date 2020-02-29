package nerd.cave.web.endpoints.api.payment

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject.mapFrom
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.model.member.WechatMember
import nerd.cave.service.member.MemberService
import nerd.cave.service.payment.PaymentService
import nerd.cave.store.ProductStoreService
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.exceptions.BadRequestException
import nerd.cave.web.exceptions.ResourceNotFoundException
import nerd.cave.web.extentions.coroutine
import nerd.cave.web.extentions.endIfOpen
import nerd.cave.web.extentions.ok
import nerd.cave.web.session.NerdCaveSessionHandler
import nerd.cave.web.session.nerdCaveMember
import nerd.cave.web.wx.HASH_ALGORITHM
import nerd.cave.web.wx.payment.PaymentSecretRetriever
import nerd.cave.web.wx.payment.WXPayApiFields
import nerd.cave.web.wx.payment.WXPayClient
import nerd.cave.web.wx.payment.toWXPaymentResponse
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.ZonedDateTime

class PaymentEndpoints(
    vertx: Vertx,
    private val clock: Clock,
    private val wxPayClient: WXPayClient,
    sessionHandler: NerdCaveSessionHandler,
    private val paymentSecretRetriever: PaymentSecretRetriever,
    private val memberService: MemberService,
    private val paymentService: PaymentService,
    private val productStoreService: ProductStoreService
): HttpEndpoint {
    companion object {
        private val logger = LoggerFactory.getLogger(PaymentEndpoints::class.java)
    }

    override val router = Router.router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        route(sessionHandler.handler)
        post("/refreshSignKey") { refreshSignKey(it) }
        post("/placeOrder") { placeOrder(it) }
        get("/info/:paymentId") { paymentInfo(it) }
    }

    private suspend fun placeOrder(ctx: RoutingContext) {
        val member = ctx.nerdCaveMember()
        val openid = (member.memberSource as? WechatMember)?.openid ?: throw BadRequestException("Only Wechat member can place order")
        val productIds = ctx.bodyAsJson.getJsonArray("products").map { it.toString() }
        val nullableProducts = productIds.map { productStoreService.fetchById(it) }
        if (!nullableProducts.all { it != null} ) throw BadRequestException("Some of the products don't exist, please check.")
        val products = nullableProducts.map { it!! }
        val canPurchase = memberService.canPurchaseOnWechat(member.id, products)
        if (!canPurchase) throw BadRequestException("Can't purchase products: $productIds")
        val remoteHost = ctx.request().connection().remoteAddress().host()
        val payment = paymentService.newPayment(member.id, openid, products)
        val prepayInfo = wxPayClient.placeUnifiedOrder(
            openid = openid,
            paymentId = payment.id,
            productBody = products.map { it.description }.toString(),
            fee = payment.totalFee,
            creatorIP = remoteHost
        ).toWXPaymentResponse()
        if(prepayInfo.isResponseSuccess) {
            val prepayId = prepayInfo[WXPayApiFields.PREPAY_ID] ?: throw BadRequestException("${WXPayApiFields.PREPAY_ID} not found in wx pay")
            if (paymentService.updatePrepay(payment.id, prepayId, prepayInfo)) {
                val timestamp = ZonedDateTime.now(clock).toEpochSecond()
                val nonceStr = wxPayClient.createNonseStr()
                val appId = wxPayClient.appId
                val `package` = "prepay_id=$prepayId"
                val signType = HASH_ALGORITHM
                val signature = wxPayClient.sign(
                    "appId" to appId,
                    "timeStamp" to timestamp.toString(),
                    "nonceStr" to nonceStr,
                    "package" to `package`,
                    "signType" to signType
                )
                ctx.response().ok(
                    jsonObjectOf(
                        "appId" to appId,
                        "paymentId" to payment.id,
                        "timeStamp" to timestamp.toString(),
                        "nonceStr" to nonceStr,
                        "prepay_id" to prepayId,
                        "signType" to signType,
                        "signature" to signature
                    )
                )
            } else {
                throw BadRequestException("Unable to locate payment")
            }
        } else {
            throw BadRequestException("WX pay request failed $prepayInfo")
        }
    }

    private suspend fun paymentInfo(ctx: RoutingContext) {
        val paymentId = ctx.request().params().get("paymentId")
        val payment = paymentService.fetchPayment(paymentId) ?: throw ResourceNotFoundException("Payment id: [$paymentId] not found")
        val member = ctx.nerdCaveMember()
        val openid = (member.memberSource as? WechatMember)?.openid ?: throw BadRequestException("Only Wechat member can place order")
        if (payment.openid != openid) {
            throw BadRequestException("Payment user not matched.")
        }
        ctx.response().endIfOpen(
            HttpResponseStatus.OK,
            mapFrom(payment).encodePrettily()
        )
    }

    private suspend fun refreshSignKey(ctx: RoutingContext) {
        paymentSecretRetriever.refreshAndRetrievePaymentSecret()
        ctx.response().endIfOpen(HttpResponseStatus.OK)
    }
}