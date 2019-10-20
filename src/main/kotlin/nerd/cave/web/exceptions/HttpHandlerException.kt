package nerd.cave.web.exceptions

import java.lang.Exception

sealed class HttpHandlerException(val statusCode: Int, msg: String): Exception(msg)

data class ForbiddenException(val msg: String): HttpHandlerException(403, msg)
data class ResourceNotFoundException(val msg:String): HttpHandlerException(404, msg)