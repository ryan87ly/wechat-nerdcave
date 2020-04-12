package nerd.cave.web.endpoints.api

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.service.holiday.HolidayService
import nerd.cave.util.toLocalDate
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.exceptions.BadRequestException
import nerd.cave.web.extentions.coroutine
import nerd.cave.web.extentions.ok
import nerd.cave.web.session.NerdCaveSessionHandler
import org.slf4j.LoggerFactory

class HolidayEndpoints(
    vertx: Vertx,
    sessionHandler: NerdCaveSessionHandler,
    private val holidayService: HolidayService
): HttpEndpoint {

    companion object {
        private val logger = LoggerFactory.getLogger(HolidayEndpoints::class.java)
    }

    override val router: Router = Router.router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        route(sessionHandler.handler)
        get("/isholiday/:date") { isHoliday(it) }
    }

    private suspend fun isHoliday(ctx: RoutingContext) {
        val date = ctx.request().params().get("date")?.toLocalDate() ?: throw BadRequestException("data is required")
        val isHoliday = holidayService.isHoliday(date)
        ctx.response().ok(
            jsonObjectOf(
                "isHoliday" to isHoliday
            )
        )
    }
}