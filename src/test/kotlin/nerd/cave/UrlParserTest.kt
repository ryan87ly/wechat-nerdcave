package nerd.cave

import com.mongodb.ServerAddress
import nerd.cave.net.toServerAddress
import nerd.cave.net.toServerList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


internal class UrlParserTest {

    @Test
    fun `Server address parse` () {
        val sa = "localhost:27017".toServerAddress()
        assertEquals("localhost", sa.host)
        assertEquals(27017, sa.port)
    }

    @Test
    fun `Server address list parse`() {
        val servers = "localhost:27017;192.168.1.12:27018".toServerList()
        assertEquals(2, servers.size)
        assertEquals(ServerAddress("localhost", 27017), servers[0])
        assertEquals(ServerAddress("192.168.1.12", 27018), servers[1])
    }
}