package nerd.cave.web.wx

import io.vertx.core.json.JsonObject

inline fun JsonObject.isSuccess(): Boolean {
    return !this.containsKey("errcode") && this.getInteger("errcode") != 0
}

