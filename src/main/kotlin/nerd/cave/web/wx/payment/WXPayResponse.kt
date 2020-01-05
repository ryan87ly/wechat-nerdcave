package nerd.cave.web.wx.payment

import nerd.cave.web.wx.payment.WXPayApiFields.RETURN_CODE
import nerd.cave.web.wx.payment.WXPayApiFields.RETURN_MSG

class WXPayResponse(private val xmlContent: Map<String, String>): Map<String, String> by xmlContent {
    companion object {
        private const val REQUEST_SUCCESS = "SUCCESS"
    }

    val isSuccess: Boolean by lazy {
        get(RETURN_CODE) == REQUEST_SUCCESS
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
    const val RETURN_MSG = "return_msg"
}

fun Map<String, String>.toWXPaymentResponse(): WXPayResponse {
    return WXPayResponse(this)
}