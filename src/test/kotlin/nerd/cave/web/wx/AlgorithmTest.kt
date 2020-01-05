package nerd.cave.web.wx

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AlgorithmTest {

    @Test
    fun testSign() {
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
}