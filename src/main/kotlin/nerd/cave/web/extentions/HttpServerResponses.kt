package nerd.cave.web.extentions

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import nerd.cave.web.exceptions.HttpHandlerException

fun HttpServerResponse.endIfOpen(httpException: HttpHandlerException) {
    putHeader("content-type", "text/plain")
    endIfOpen(httpException.statusCode, httpException.message)
}

fun HttpServerResponse.ok(content: JsonArray) {
    putHeader("content-type", "application/json")
    endIfOpen(HttpResponseStatus.OK, content.encodePrettily())
}

fun HttpServerResponse.ok(content: JsonObject) {
    putHeader("content-type", "application/json")
    endIfOpen(HttpResponseStatus.OK, content.encodePrettily())
}

fun HttpServerResponse.respondCSV(content: String, fileName: String) {
    putHeader("Content-Type", "text/csv")
    putHeader("Content-Disposition", "attachment; filename=\"$fileName\"")
    end(content)
}

fun HttpServerResponse.endIfOpen(statusCode: HttpResponseStatus, msg: String? = null) {
    endIfOpen(statusCode.code(), msg)
}

private fun HttpServerResponse.endIfOpen(statusCode: Int, msg: String?) {
    if (!this.closed()) {
        setStatusCode(statusCode)
        if (msg != null) end(msg) else end()
    }
}

