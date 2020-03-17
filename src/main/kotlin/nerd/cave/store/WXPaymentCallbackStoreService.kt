package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.api.order.wechat.WXPaymentCallback

interface WXPaymentCallbackStoreService: LifeCycle {
    suspend fun insertNotification(paymentCallback: WXPaymentCallback)
}