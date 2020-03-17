package nerd.cave.service.order

import nerd.cave.model.admin.Account
import nerd.cave.model.admin.Right
import nerd.cave.model.admin.hasRight
import nerd.cave.model.api.branch.Branch
import nerd.cave.model.api.member.*
import nerd.cave.model.api.order.EnrichedApprovalInfo
import nerd.cave.model.api.order.EnrichedOrder
import nerd.cave.model.api.order.OrderStatus
import nerd.cave.model.api.order.OrderType
import nerd.cave.model.api.order.offline.ApprovalInfo
import nerd.cave.model.api.order.offline.OfflineOrder
import nerd.cave.model.api.order.offline.OfflineOrderItem
import nerd.cave.model.api.order.offline.OfflineOrderStatus
import nerd.cave.model.api.order.wechat.PaymentItem
import nerd.cave.model.api.order.wechat.PaymentStatus
import nerd.cave.model.api.order.wechat.WXPayment
import nerd.cave.model.api.product.*
import nerd.cave.model.api.ticket.Ticket
import nerd.cave.model.api.ticket.TicketStatus
import nerd.cave.service.holiday.HolidayService
import nerd.cave.store.StoreService
import nerd.cave.util.MongoIdGenerator
import nerd.cave.web.exceptions.BadRequestException
import nerd.cave.web.exceptions.ForbiddenException
import nerd.cave.web.exceptions.InternalServerErrorException
import nerd.cave.web.wx.payment.WXPayResponse
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

class OrderServiceImpl(private val clock: Clock, storeService: StoreService, holidayService: HolidayService): OrderService {
    companion object {
        private val logger = LoggerFactory.getLogger(OrderServiceImpl::class.java)
    }
    private val offlineOrderStoreService by lazy { storeService.offlineOrderStoreService }
    private val productStoreService by lazy { storeService.productStoreService }
    private val memberStoreService by lazy { storeService.memberStoreService }
    private val branchStoreService by lazy { storeService.branchStoreService }
    private val paymentStoreService by lazy { storeService.WXPaymentStoreService }
    private val ticketStoreService by lazy { storeService.ticketStoreService }
    private val adminAccountStoreService by lazy { storeService.adminAccountStoreService }
    private val priceResolver = ProductPriceResolver(clock, holidayService)
    private val idGenerator = MongoIdGenerator()

    override suspend fun newOfflineOrder(memberId: String, product: Product, branch: Branch): OfflineOrder {
        val id = idGenerator.nextId()
        val orderItem = OfflineOrderItem(
            product.id,
            product.productType,
            product.detail,
            product.shortDescription
        )
        val order = OfflineOrder(
            id,
            memberId,
            ZonedDateTime.now(clock),
            orderItem,
            branch.id,
            OfflineOrderStatus.UNAPPROVED
        )
        offlineOrderStoreService.insertOrder(order)
        return order
    }

    override suspend fun approveOfflineOrder(orderId: String, approver: Account) {
        val order = offlineOrderStoreService.fetchOrder(orderId) ?: throw BadRequestException("No offline order found [$orderId]")
        if (order.status == OfflineOrderStatus.APPROVED) throw BadRequestException("Order [$orderId] has already been approved")
        val member = memberStoreService.fetchById(order.memberId) ?: throw BadRequestException("Member [${order.memberId}] not found!")
        if (!approver.hasRight(Right.APPROVE_OFFLINE_ORDER)) throw ForbiddenException("Account [${approver.nickname}] is not allowed to approve offline order")
        val approvalInfo = ApprovalInfo(approver.id, ZonedDateTime.now(clock))
        if (offlineOrderStoreService.approveOrder(orderId, approvalInfo)) {
            redeemOfflineProduct(member, order.item.detail)
        } else {
            throw BadRequestException("Order [$orderId] has already been approved")
        }
    }

    override suspend fun orders(startLocalTimeInclusive: LocalDateTime?, endLocalTimeExclusive: LocalDateTime?): List<EnrichedOrder> {
        val startDateTimeInclusive = startLocalTimeInclusive?.atZone(clock.zone)
        val endDateTimeExclusive = endLocalTimeExclusive?.atZone(clock.zone)
        val offlineOrders = offlineOrderStoreService.fetchOrders(startDateTimeInclusive, endDateTimeExclusive)
            .map{ it.toEnrichedOrder() }
        val wxOrders = paymentStoreService.fetchPayments(startDateTimeInclusive, endDateTimeExclusive)
            .flatMap { it.toEnrichedOrders() }
        return offlineOrders.plus(wxOrders)
            .sortedBy { it.time }
    }

    private suspend fun OfflineOrder.toEnrichedOrder(): EnrichedOrder {
        val member = memberStoreService.fetchById(memberId)
        val memberName = member?.memberContact?.legalName?:"<id-$memberId>"
        val contactNumber = member?.memberContact?.contactNumber?:"<NO_CONTACT>"
        val branchName = branchStoreService.fetchById(branchId)?.name?:"id-${branchId}"
        return EnrichedOrder(
            id,
            memberName,
            contactNumber,
            time.withZoneSameInstant(clock.zone).toLocalDateTime(),
            item.productType,
            item.description,
            branchName,
            OrderType.OFFLINE,
            OrderStatus.fromOfflineOrderStatus(status),
            approvalInfo?.toEnrichedApporvalInfo()
        )
    }

    private suspend fun ApprovalInfo.toEnrichedApporvalInfo(): EnrichedApprovalInfo {
        val approverName = adminAccountStoreService.findById(approverId)?.nickname?:"<id-$approverId>"
        val approvalTime = time.withZoneSameInstant(clock.zone).toLocalDateTime()
        return EnrichedApprovalInfo(
            approverId,
            approverName,
            approvalTime
        )
    }

    private suspend fun WXPayment.toEnrichedOrders(): List<EnrichedOrder> {
        val member = memberStoreService.fetchById(memberId)
        val memberName = member?.memberContact?.legalName?:"<id-$memberId>"
        val contactNumber = member?.memberContact?.contactNumber?:"<NO_CONTACT>"
        val branchName = branchStoreService.fetchById(branchId)?.name?:"id-${branchId}"
        return items.map {
            EnrichedOrder(
                id,
                memberName,
                contactNumber,
                time.withZoneSameInstant(clock.zone).toLocalDateTime(),
                it.productType,
                it.description,
                branchName,
                OrderType.WECHAT,
                OrderStatus.fromWXPaymentOrderStatus(status)
            )
        }
    }

    private suspend fun redeemOfflineProduct(member: Member, productDetail: ProductDetail) {
        val now = LocalDate.now(clock)
        val updateMemberDetail = when(productDetail) {
            is MultiEntriesProduct -> {
                val expiryDate = now.plusMonths(productDetail.validMonths.toLong()).plusDays(-1)
                when(val currentMemberDetail = member.memberDetail) {
                    is MultiEntriesMember -> MultiEntriesMember(
                        usedEntries = currentMemberDetail.usedEntries,
                        totalEntries = currentMemberDetail.totalEntries + productDetail.entries,
                        validFrom = now,
                        validUntil = expiryDate
                    )
                    else -> MultiEntriesMember(0, productDetail.entries, now, expiryDate)
                }
            }
            is YearlyMemberProduct -> {
                val expiryDate = now.plusYears(productDetail.years.toLong()).plusDays(-1)
                YearlyMember(now, expiryDate)
            }
            is MonthlyMemberProduct -> {
                val expiryDate = now.plusMonths(productDetail.months.toLong()).plusDays(-1)
                MonthlyMember(now, expiryDate)
            }
            else -> member.memberDetail
        }
        memberStoreService.updateMemberDetail(member.id, updateMemberDetail)
    }

    override suspend fun newPayment(memberId: String, openid: String, branchId: String, products: List<Product>): WXPayment {
        val id = idGenerator.nextId()
        val items = products.map { PaymentItem(it.id, it.productType, it.shortDescription, priceResolver.getPrice(it)) }
        val totalFee = items.map { it.itemFee }
            .sum()
        val now = ZonedDateTime.now(clock)
        val payment = WXPayment(
            id = id,
            memberId = memberId,
            openid = openid,
            time = now,
            items = items,
            totalFee = totalFee,
            branchId = branchId,
            status = PaymentStatus.INITIAL
        )
        paymentStoreService.createPayment(payment)
        return payment
    }

    override suspend fun updatePrepay(paymentId: String, prepayId: String, prepayInfo: WXPayResponse): Boolean {
        return paymentStoreService.updatePrepay(paymentId, prepayId, prepayInfo)
    }

    override suspend fun fetchPayment(paymentId: String): WXPayment? {
        return paymentStoreService.fetchPayment(paymentId)
    }

    override suspend fun redeemPayment(WXPayment: WXPayment, transactionId:String, paymentCallback: WXPayResponse) {
        val products = WXPayment.items.map { it.productId to productStoreService.fetchById(it.productId) }
        if (!products.all { it.second != null }) {
            val nullProducts = products.filter { it.second == null }
            logger.error("Can't find products ${nullProducts.map { it.first }}")
            throw InternalServerErrorException("Can't find products ${nullProducts.map { it.first }}")
        }
        if (!paymentStoreService.updateRedeemedPayment(WXPayment.id, transactionId, paymentCallback)) {
            logger.error("Payment is not in ${PaymentStatus.PREPAY} state")
            throw InternalServerErrorException("Payment is not in ${PaymentStatus.PREPAY} state")
        }
        redeemProducts(WXPayment, products.map { it.second!! })
    }

    private suspend fun redeemProducts(WXPayment: WXPayment, products: List<Product>) {
        val hasSingleEntryFee = products.any { it.productType == ProductType.SINGLE_ENTRY_FEE }
        if (!hasSingleEntryFee) {
            throw InternalServerErrorException("No SINGLE_ENTRY_FEE found, nothing to redeem")
        }
        val hasEquipment = products.any { it.productType == ProductType.EQUIPMENT_RENTAL_FEE }
        val time = ZonedDateTime.now(clock)
        val ticket = Ticket(
            id = idGenerator.nextId(),
            paymentId = WXPayment.id,
            memberId = WXPayment.memberId,
            purchaseTime = time,
            hasEquipment = hasEquipment,
            status = TicketStatus.NOT_USED,
            tokenId = null
        )
        ticketStoreService.insertTicket(ticket)
    }

}