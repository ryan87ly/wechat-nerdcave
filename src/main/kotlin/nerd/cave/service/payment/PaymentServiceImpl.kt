package nerd.cave.service.payment

import nerd.cave.model.payment.Payment
import nerd.cave.model.payment.PaymentItem
import nerd.cave.model.payment.PaymentStatus
import nerd.cave.model.product.Product
import nerd.cave.model.product.ProductType
import nerd.cave.model.ticket.Ticket
import nerd.cave.model.ticket.TicketStatus
import nerd.cave.store.StoreService
import nerd.cave.util.MongoIdGenerator
import nerd.cave.web.exceptions.InternalServerErrorException
import nerd.cave.web.wx.payment.WXPayResponse
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.ZonedDateTime

class PaymentServiceImpl(private val storeService: StoreService, private val clock: Clock): PaymentService {
    companion object {
        private val logger = LoggerFactory.getLogger(PaymentServiceImpl::class.java)
    }
    private val paymentStoreService by lazy { storeService.paymentStoreService }
    private val ticketStoreService by lazy { storeService.ticketStoreService }
    private val productStoreService by lazy { storeService.productStoreService }
    private val idGenerator = MongoIdGenerator()

    override suspend fun newPayment(memberId: String, openid: String, products: List<Product>): Payment {
        val id = idGenerator.nextId()
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

    override suspend fun fetchPayment(paymentId: String): Payment? {
        return paymentStoreService.fetchPayment(paymentId)
    }

    override suspend fun redeemPayment(payment: Payment, transactionId:String, paymentCallback: WXPayResponse) {
        val products = payment.items.map { it.productId to productStoreService.fetchById(it.productId) }
        if (!products.all { it.second != null }) {
            val nullProducts = products.filter { it.second == null }
            logger.error("Can't find products ${nullProducts.map { it.first }}")
            throw InternalServerErrorException("Can't find products ${nullProducts.map { it.first }}")
        }
        if (!paymentStoreService.updateRedeemedPayment(payment.id, transactionId, paymentCallback)) {
            logger.error("Payment is not in ${PaymentStatus.Prepay} state")
            throw InternalServerErrorException("Payment is not in ${PaymentStatus.Prepay} state")
        }
        redeemProducts(payment, products.map { it.second!! })
    }

    private suspend fun redeemProducts(payment: Payment, products: List<Product>) {
        val hasSingleEntryFee = products.any { it.productType == ProductType.SINGLE_ENTRY_FEE }
        if (!hasSingleEntryFee) {
            throw InternalServerErrorException("No SINGLE_ENTRY_FEE found, nothing to redeem")
        }
        val hasEquipment = products.any { it.productType == ProductType.EQUIPMENT_RENTAL_FEE }
        val time = ZonedDateTime.now(clock)
        val ticket = Ticket(
            id = idGenerator.nextId(),
            paymentId = payment.id,
            memberId = payment.memberId,
            purchaseTime = time,
            hasEquipment = hasEquipment,
            status = TicketStatus.NOT_USED,
            tokenId = null
        )
        ticketStoreService.insertTicket(ticket)
    }

}