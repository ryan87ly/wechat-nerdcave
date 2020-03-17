package nerd.cave.store.mongo

import com.mongodb.client.model.IndexOptions
import nerd.cave.model.api.order.wechat.PaymentStatus
import nerd.cave.model.api.order.wechat.WXPayment
import nerd.cave.store.WXPaymentStoreService
import nerd.cave.web.wx.payment.WXPayApiFields.TIME_END
import nerd.cave.web.wx.payment.WXPayResponse
import org.litote.kmongo.*
import java.time.ZonedDateTime

class MongoWXPaymentStoreService(mongoStoreService: MongoStoreService): WXPaymentStoreService {
    private val collection by lazy { mongoStoreService.getCollection<WXPayment>() }

    override suspend fun start() {
        collection.ensureIndex("id" eq 1, IndexOptions().unique(true))
    }

    override suspend fun createPayment(WXPayment: WXPayment) {
        collection.insertOne(WXPayment)
    }

    override suspend fun updatePrepay(paymentId: String, prepayId: String, prepayInfo: WXPayResponse): Boolean {
        val query = and(
            "id" eq paymentId,
            "status" eq PaymentStatus.INITIAL
        )
        val update = combine(
            setValue(WXPayment::wxPrepayId, prepayId),
            setValue(WXPayment::status, PaymentStatus.PREPAY),
            setValue(WXPayment::wxPrepayInfo, prepayInfo)
        )
        return collection.updateOne(query, update).succeedUpdateOne()
    }

    override suspend fun fetchPayment(paymentId: String): WXPayment? {
        val query = "id" eq paymentId
        return collection.findOne(query)
    }

    override suspend fun updateRedeemedPayment(paymentId: String, transactionId: String, paymentCallback: WXPayResponse): Boolean {
        val query = and(
            "id" eq paymentId,
            "status" eq PaymentStatus.PREPAY
        )
        val paymentTime = paymentCallback[TIME_END]
        val update = combine(
            setValue(WXPayment::status, PaymentStatus.COMPLETED),
            setValue(WXPayment::wxTransactionId, transactionId),
            setValue(WXPayment::wxPaymentCallback, paymentCallback),
            setValue(WXPayment::wxPaymentEndTime, paymentTime)
        )
        return collection.updateOne(query, update).succeedUpdateOne()
    }

    override suspend fun fetchPayments(startTimeInclusive: ZonedDateTime?, endTimeExclusive: ZonedDateTime?): List<WXPayment> {
        val baseQuery = WXPayment::status ne PaymentStatus.INITIAL
        val query = if(startTimeInclusive != null && endTimeExclusive != null) {
            and(
                baseQuery,
                WXPayment::time gte startTimeInclusive,
                WXPayment::time lt endTimeExclusive
            )
        } else if (startTimeInclusive != null && endTimeExclusive == null) {
            baseQuery and (WXPayment::time gte startTimeInclusive)
        } else if (startTimeInclusive == null && endTimeExclusive != null) {
            baseQuery and (WXPayment::time lt endTimeExclusive)
        } else baseQuery
        return collection.find(query).toList()
    }


}