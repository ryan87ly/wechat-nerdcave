package nerd.cave.service.payment

import nerd.cave.model.payment.Payment
import nerd.cave.model.payment.PaymentItem
import nerd.cave.model.payment.PaymentStatus
import nerd.cave.model.product.Product
import nerd.cave.store.StoreService
import nerd.cave.web.wx.payment.WXPayResponse
import org.bson.types.ObjectId
import java.util.*

class PaymentServiceImpl(private val storeService: StoreService): PaymentService {
    private val paymentStoreService by lazy { storeService.paymentStoreService }

    override suspend fun newPayment(memberId: String, openid: String, products: List<Product>): Payment {
        val id = ObjectId.get().toString()
        val items = products.map { PaymentItem(it.id, it.fee) }
        val totalFee = items.map { it.itemFee }
            .sum()
        val payment = Payment(
            id = id,
            memberId = memberId,
            openid = openid,
            items = items,
            totalFee = totalFee,
            status = PaymentStatus.Initial
        )
        paymentStoreService.createPayment(payment)
        return payment
    }

    override suspend fun updatePrepay(paymentId: String, prepayId: String, prepayInfo: WXPayResponse): Boolean {
        return paymentStoreService.updatePrepay(paymentId, prepayId, prepayInfo)
    }
}