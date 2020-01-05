package nerd.cave.store.mongo

import com.mongodb.client.model.IndexOptions
import nerd.cave.model.payment.Payment
import nerd.cave.model.payment.PaymentStatus
import nerd.cave.store.PaymentStoreService
import nerd.cave.web.wx.payment.WXPayResponse
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.setValue

class MongoPaymentStoreService(mongoStoreService: MongoStoreService): PaymentStoreService {
    private val collection = mongoStoreService.getCollection<Payment>()

    override suspend fun start() {
        collection.ensureIndex("id" eq 1, IndexOptions().unique(true))
    }

    override suspend fun createPayment(payment: Payment) {
        collection.insertOne(payment)
    }

    override suspend fun updatePrepay(paymentId: String, prepayId: String, prepayInfo: WXPayResponse): Boolean {
        val query = and(
            "id" eq paymentId,
            "status" eq PaymentStatus.Initial
        )
        val update = combine(
            setValue(Payment::wxPrepayId, prepayId),
            setValue(Payment::status, PaymentStatus.Prepay),
            setValue(Payment::wxPrepayInfo, prepayInfo)
        )
        return collection.updateOne(query, update).modifiedCount == 1L
    }

}