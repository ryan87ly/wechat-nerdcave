package nerd.cave.web.extentions

import io.vertx.core.json.JsonObject
import nerd.cave.web.exceptions.BadRequestException

fun JsonObject.getMandatoryString(fieldName: String, msgPrefix: String? = null): String {
    return this.getString(fieldName) ?: throw BadRequestException("${msgPrefix?:""}$fieldName could not be found in request body")
}

fun JsonObject.getMandatoryInt(fieldName: String, msgPrefix: String? = null): Int {
    return this.getInteger(fieldName) ?: throw BadRequestException("${msgPrefix?:""}$fieldName could not be found in request body")
}

fun JsonObject.getMandatoryBoolean(fieldName: String, msgPrefix: String? = null): Boolean {
    return this.getBoolean(fieldName) ?: throw BadRequestException("${msgPrefix?:""}$fieldName could not be found in request body")
}

fun JsonObject.getMandatoryJsonObject(fieldName: String, msgPrefix: String? = null): JsonObject {
    return this.getJsonObject(fieldName) ?: throw BadRequestException("${msgPrefix?:""}$fieldName could not be found in request body")
}