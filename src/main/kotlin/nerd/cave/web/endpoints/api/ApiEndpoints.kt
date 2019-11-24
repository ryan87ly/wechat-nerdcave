package nerd.cave.web.endpoints.api

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import nerd.cave.web.client.WebClient
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.wx.WXWebClient

class ApiEndpoints (vertx: Vertx, wxWebClient: WXWebClient): HttpEndpoint {
    override val router = Router.router(vertx).apply {
        mountSubRouter("/user", UserEndpoints(vertx, wxWebClient).router)
    }
}