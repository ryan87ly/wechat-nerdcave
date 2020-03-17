package nerd.cave.store.mongo

import nerd.cave.model.api.order.wechat.WXPaymentCallback
import nerd.cave.store.WXPaymentCallbackStoreService

class MongoWXPaymentCallbackStoreService(mongoStoreService: MongoStoreService) : WXPaymentCallbackStoreService {
    private val collection by lazy { mongoStoreService.getCollection<WXPaymentCallback>() }

    override suspend fun insertNotification(paymentCallback: WXPaymentCallback) {
        collection.insertOne(paymentCallback)
    }

}