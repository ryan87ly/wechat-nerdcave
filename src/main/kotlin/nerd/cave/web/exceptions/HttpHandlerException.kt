package nerd.cave.web.exceptions

import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpResponseStatus.*
import java.lang.Exception

sealed class HttpHandlerException(val statusCode: HttpResponseStatus, msg: String?): Exception(msg)

data class BadRequestException(val msg: String?): HttpHandlerException(BAD_REQUEST, msg)
data class UnauthorizedException(val msg: String?): HttpHandlerException(UNAUTHORIZED, msg)
data class ForbiddenException(val msg: String?): HttpHandlerException(FORBIDDEN, msg)
data class ResourceNotFoundException(val msg:String?): HttpHandlerException(NOT_FOUND, msg)
data class InternalServerErrorException(val msg:String?): HttpHandlerException(INTERNAL_SERVER_ERROR, msg)