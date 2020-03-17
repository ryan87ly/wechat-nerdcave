package nerd.cave.web.wx

import nerd.cave.xml.fromXmlToMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AlgorithmTest {

    @Test
    fun `test sign`() {
        assertEquals("9A0A8659F005D6984697E2CA0A9CF3B7",
            sign("192006250b4c09247ec02edce69f6a2d",
                "appid" to "wxd930ea5d5a258f4f",
                "device_info" to "1000",
                "mch_id" to "10000100",
                "body" to "test",
                "nonce_str" to "ibuaiVcKdpRxkhJA"
            )
        )
    }

    @Test
    fun `test verify payment callback`() {
        val response = """
            <xml><appid><![CDATA[wx96fb791b0725aa1c]]></appid>
            <bank_type><![CDATA[OTHERS]]></bank_type>
            <cash_fee><![CDATA[1]]></cash_fee>
            <fee_type><![CDATA[CNY]]></fee_type>
            <is_subscribe><![CDATA[N]]></is_subscribe>
            <mch_id><![CDATA[1561613351]]></mch_id>
            <nonce_str><![CDATA[965282897]]></nonce_str>
            <openid><![CDATA[o7tbT5GlL8DhAOifK0DmBXkDto-s]]></openid>
            <out_trade_no><![CDATA[5e173b172b3a5f6a85d50e1c]]></out_trade_no>
            <result_code><![CDATA[SUCCESS]]></result_code>
            <return_code><![CDATA[SUCCESS]]></return_code>
            <sign><![CDATA[E0180CB21C19CB24CED54475C7932B71]]></sign>
            <time_end><![CDATA[20200109224020]]></time_end>
            <total_fee>1</total_fee>
            <trade_type><![CDATA[JSAPI]]></trade_type>
            <transaction_id><![CDATA[4200000485202001098217354983]]></transaction_id>
            </xml>
        """.trimIndent()
        val responseMap = response.fromXmlToMap()
        val signature = responseMap["sign"]
        val s = sign(
                "4bf733f8ab86ee3a177c39c938db6c32",
                * responseMap.filterNot { it.key == "sign" }
                    .map { it.key to it.value }
                    .toTypedArray()
            )
        assertEquals(
            signature,
            sign(
                "4bf733f8ab86ee3a177c39c938db6c32",
                * responseMap.filterNot { it.key == "sign" }
                    .map { it.key to it.value }
                    .toTypedArray()
            )
        )
    }

    @Test
    fun `verify md5`() {
        assertEquals("527bd5b5d689e2c32ae974c6229ff785".toUpperCase(), "john".toMD5())
    }
}