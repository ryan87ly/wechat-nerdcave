package nerd.cave.web.client

import io.vertx.core.json.JsonObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class WebClient (val executor: Executor) {
    private lateinit var httpClient: HttpClient

    fun start() {
        httpClient = HttpClient.newBuilder()
        .executor(executor)
        .build()
    }

    suspend fun get(
        uri: String
    ): JsonObject {
        return suspendCoroutine { con ->
            val request = HttpRequest.newBuilder()
                .uri(URI(uri))
                .GET()
                .build()
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete { r, err ->
                    print("Http [Get] from $uri, response: $r, err: $err")
                    if (err == null) {
                        con.resume(JsonObject(r.body()))
                    } else {
                        con.resumeWithException(err)
                    }
                }
        }
    }
}