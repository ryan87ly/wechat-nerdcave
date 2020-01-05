package nerd.cave.store

import nerd.cave.component.LifeCycle

interface StoreService: LifeCycle {
    val memberStoreService: MemberStoreService
    val sessionStoreService: SessionStoreService
    val productStoreService: ProductStoreService
    val paymentStoreService: PaymentStoreService
}