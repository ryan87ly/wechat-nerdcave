package nerd.cave.web.extentions

import io.vertx.ext.web.Router
import kotlinx.coroutines.CoroutineDispatcher
import nerd.cave.web.CoroutineRouter

inline fun Router.coroutine(dispatcher: CoroutineDispatcher): CoroutineRouter {
    return CoroutineRouter(dispatcher, this)
}