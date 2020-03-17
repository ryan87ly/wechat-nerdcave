package nerd.cave.service.order

import nerd.cave.model.api.order.wechat.WXPayment
import nerd.cave.model.api.order.wechat.PaymentStatus

fun WXPayment.hasProceeded(): Boolean {
    return this.status == PaymentStatus.COMPLETED
}