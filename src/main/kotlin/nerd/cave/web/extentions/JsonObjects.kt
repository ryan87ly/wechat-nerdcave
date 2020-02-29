package nerd.cave.web.extentions

import io.vertx.core.json.JsonObject
import nerd.cave.web.exceptions.BadRequestException

fun JsonObject.getMandatoryString(fieldName: String): String {
    return this.getString(fieldName) ?: throw BadRequestException("$fieldName could not be found in request body")
}