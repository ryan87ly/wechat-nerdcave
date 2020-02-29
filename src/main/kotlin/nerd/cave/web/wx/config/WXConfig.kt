package nerd.cave.web.wx.config

import nerd.cave.Environment

sealed class WXConfig(val paymentHost: String, val notifyUrl: String) {
    companion object {
        private const val APP_ID_KEY = "WECHAT_APP_ID"
        private const val APP_SECRET_KEY = "WECHAT_APP_SECRET"
        private const val APP_MCH_ID = "WECHAT_MCH_ID"
        private const val APP_PAYMENT_SECRET_KEY = "WECHAT_PAYMENT_SECRET"

        fun forEnv(env: Environment): WXConfig {
            return when(env) {
                Environment.LOCAL -> Local
                Environment.UAT -> UAT
                Environment.PROD -> Prod
            }
        }
    }

    val appId = System.getenv(APP_ID_KEY)
    val appSecret = System.getenv(APP_SECRET_KEY)
    val mchId = System.getenv(APP_MCH_ID)
    val apiHost = "https://api.weixin.qq.com"
    val paymentSecret = System.getenv(APP_PAYMENT_SECRET_KEY)
}

object Local: WXConfig(
    "https://api.mch.weixin.qq.com/sandboxnew/pay",
    "https://www.nerdcave.club/api/notification/payment") {
}

object UAT: WXConfig(
    "https://api.mch.weixin.qq.com/sandboxnew/pay",
    "https://www.nerdcave.club/api/notification/payment") {
}

object Prod: WXConfig("https://api.mch.weixin.qq.com/pay", "https://www.nerdcave.club/api/notification/payment")