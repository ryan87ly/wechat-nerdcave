package nerd.cave.service.payment

import nerd.cave.model.payment.Payment
import nerd.cave.model.payment.PaymentStatus

fun Payment.hasProceeded(): Boolean {
    return this.status == PaymentStatus.Completed
}