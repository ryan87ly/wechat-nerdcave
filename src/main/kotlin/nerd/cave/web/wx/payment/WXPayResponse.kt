package nerd.cave.web.wx.payment

import nerd.cave.web.wx.payment.WXPayApiFields.RESULT_CODE
import nerd.cave.web.wx.payment.WXPayApiFields.RETURN_CODE
import nerd.cave.web.wx.payment.WXPayApiFields.RETURN_MSG
import nerd.cave.web.wx.payment.WXPayApiFields.SIGN
import nerd.cave.web.wx.sign

class WXPayResponse(private val xmlContent: Map<String, String>): Map<String, String> by xmlContent {
    companion object {
        private const val SUCCESS = "SUCCESS"
        private const val FAIL = "FAIL"
    }

    val isResponseSuccess: Boolean by lazy {
        get(RETURN_CODE) == SUCCESS
    }

    val isPaymentSuccess: Boolean by lazy {
        get(RESULT_CODE) == SUCCESS
    }

    val message: String by lazy {
        get(RETURN_MSG) ?: "NO_MESSAGE_FOUND"
    }

    override fun toString(): String {
        return xmlContent.toString()
    }
}

object WXPayApiFields {
    const val PREPAY_ID = "prepay_id"
    const val RETURN_CODE = "return_code"
    const val RESULT_CODE = "result_code"
    const val RETURN_MSG = "return_msg"
    const val SIGN = "sign"
    const val OUT_TRADE_NO = "out_trade_no" // NC PaymentId
    const val TRANSACTION_ID = "transaction_id"
    const val TIME_END = "time_end"
    const val TOTAL_FEE = "total_fee"
}

fun Map<String, String>.toWXPaymentResponse(): WXPayResponse {
    return WXPayResponse(this)
}

fun WXPayResponse.verifyContent(appSecret: String): Boolean {
    val signature = this[SIGN]
    val expectedSignature = sign(
        appSecret,
        * this.filterNot { it.key == "sign" }
            .map { it.key to it.value }
            .toTypedArray()
    )
    return signature == expectedSignature
}