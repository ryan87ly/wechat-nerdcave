package nerd.cave.web.extentions

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import nerd.cave.web.exceptions.HttpHandlerException

fun HttpServerResponse.endIfOpen(content: JsonObject) {
    putHeader("content-type", "application/json")
    endIfOpen(HttpResponseStatus.OK, content.encodePrettily())
}

fun HttpServerResponse.endIfOpen(content: JsonArray) {
    putHeader("content-type", "application/json")
    endIfOpen(HttpResponseStatus.OK, content.encodePrettily())
}

fun HttpServerResponse.endIfOpen(httpException: HttpHandlerException) {
    putHeader("content-type", "text/plain")
    endIfOpen(httpException.statusCode, httpException.message)
}

fun HttpServerResponse.endIfOpen(statusCode: HttpResponseStatus, msg: String? = null) {
    endIfOpen(statusCode.code(), msg)
}

private fun HttpServerResponse.endIfOpen(statusCode: Int, msg: String?) {
    if (!this.closed()) {
        setStatusCode(statusCode)
        end(msg)
    }
}

