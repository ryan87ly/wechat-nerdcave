package nerd.cave.store

import nerd.cave.component.LifeCycle

interface StoreService: LifeCycle {
    val memberStoreService: MemberStoreService
    val memberEventStoreService: MemberEventStoreService
    val sessionStoreService: SessionStoreService
    val productStoreService: ProductStoreService
    val branchStoreService: BranchStoreService
    val paymentStoreService: PaymentStoreService
    val ticketStoreService: TicketStoreService
    val tokenStoreService: TokenStoreService
    val wxPaymentCallbackStoreService: WXPaymentCallbackStoreService
    val disclaimerStoreService: DisclaimerStoreService
}