package nerd.cave.web.client

import io.vertx.core.json.JsonObject
import nerd.cave.component.LifeCycle
import nerd.cave.xml.fromXmlToMap
import nerd.cave.xml.toXmlString
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class WebClient (val executor: Executor): LifeCycle {
    private lateinit var httpClient: HttpClient

    companion object {
        private val logger = LoggerFactory.getLogger(WebClient::class.java)
    }

    override suspend fun start() {
        httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .executor(executor)
            .build()
    }

    suspend fun getJson(
        uri: String
    ): JsonObject {
        return suspendCoroutine { con ->
            val request = HttpRequest.newBuilder()
                .uri(URI(uri))
                .GET()
                .build()
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete { r, err ->
                    logger.debug("Http [Get] from $uri, response: ${r.body()}, err: $err")
                    if (err == null) {
                        con.resume(JsonObject(r.body()))
                    } else {
                        con.resumeWithException(err)
                    }
                }
        }
    }

    suspend fun postXml(
        uri: String,
        body: Map<*, *>
    ): Map<String, String> {
        return suspendCoroutine { con ->
            val request = HttpRequest.newBuilder()
                .uri(URI(uri))
                .POST(HttpRequest.BodyPublishers.ofString(body.toXmlString(), StandardCharsets.UTF_8))
                .build()
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete { r, err ->
                    logger.debug("Http [POST] from $uri, body: $body, response: ${r.body()}, err: $err")
                    if (err == null) {
                        con.resume(r.body().fromXmlToMap())
                    } else {
                        con.resumeWithException(err)
                    }
                }
        }
    }
}