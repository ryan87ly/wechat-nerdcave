package nerd.cave.web.extentions

import io.vertx.core.http.HttpServerResponse

inline fun HttpServerResponse.endIfOpen(statusCode: Int, msg: String?) {
    if (!this.closed()) {
        setStatusCode(statusCode)
        end(msg)
    }
}