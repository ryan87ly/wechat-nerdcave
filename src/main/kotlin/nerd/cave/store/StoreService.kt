package nerd.cave.store

import nerd.cave.component.LifeCycle

interface StoreService: LifeCycle {
    val memberStoreService: MemberStoreService
    val memberEventStoreService: MemberEventStoreService
    val sessionStoreService: SessionStoreService
    val productStoreService: ProductStoreService
    val branchStoreService: BranchStoreService
    val WXPaymentStoreService: WXPaymentStoreService
    val ticketStoreService: TicketStoreService
    val tokenStoreService: TokenStoreService
    val wxPaymentCallbackStoreService: WXPaymentCallbackStoreService
    val disclaimerStoreService: DisclaimerStoreService
    val offlineOrderStoreService: OfflineOrderStoreService
    val adminAccountStoreService: AdminAccountStoreService
    val adminSessionStoreService: AdminSessionStoreService
    val publicHolidayStoreService: PublicHolidayStoreService
    val branchOpenStatusStoreService: BranchOpenStatusStoreService
}