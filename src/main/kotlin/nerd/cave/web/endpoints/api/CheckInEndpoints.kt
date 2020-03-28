package nerd.cave.web.endpoints.api

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.service.branch.BranchService
import nerd.cave.service.checkin.CheckInService
import nerd.cave.store.StoreService
import nerd.cave.util.toLocalDate
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.exceptions.BadRequestException
import nerd.cave.web.exceptions.ResourceNotFoundException
import nerd.cave.web.extentions.coroutine
import nerd.cave.web.extentions.getMandatoryString
import nerd.cave.web.extentions.ok
import nerd.cave.web.session.NerdCaveSessionHandler
import nerd.cave.web.session.nerdCaveMember
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

class CheckInEndpoints (
    vertx: Vertx,
    sessionHandler: NerdCaveSessionHandler,
    storeService: StoreService,
    private val checkInService: CheckInService
): HttpEndpoint {
    companion object {
        private val logger = LoggerFactory.getLogger(CheckInEndpoints::class.java)
    }

    private val memberStoreService = storeService.memberStoreService

    override val router: Router = Router.router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        route(sessionHandler.handler)
        post("/") { checkIn(it) }
        get("/history/:startDate") { checkInHistory(it) }
        get("/history/:startDate/:endDate") { checkInHistory(it) }
        get("/token/:checkInDate") { checkInToken(it) }
        get("/ranking/:startDate") { checkInRanking(it) }
        get("/ranking/:startDate/:endDate") { checkInRanking(it) }
        get("/currentnumber/:branchId") { currentBranchPeopleNumber(it) }
    }

    private suspend fun checkIn(ctx: RoutingContext) {
        val member = ctx.nerdCaveMember()
        val branchId = ctx.bodyAsJson.getMandatoryString("branchId")
        val token = checkInService.checkIn(member, branchId)
        ctx.response()
            .ok(
                jsonObjectOf(
                   "checkInNumber" to token.checkInNumber,
                   "hasEquipment" to token.hasEquipment
                )
            )
    }

    private suspend fun checkInHistory(ctx: RoutingContext) {
        val member = ctx.nerdCaveMember()
        val startDate = ctx.request().params().get("startDate")?.toLocalDate() ?: throw BadRequestException("startDate is required but missing in request path")
        val endDate = ctx.request().params().get("endDate")?.toLocalDate()
        val history = checkInService.checkInHistory(member.id, startDate, endDate)
        ctx.response()
            .ok(
                jsonObjectOf(
                    "history" to history
                )
            )
    }

    private suspend fun checkInToken(ctx: RoutingContext) {
        val member = ctx.nerdCaveMember()
        val checkInDate = ctx.request().params().get("checkInDate")?.toLocalDate() ?: throw BadRequestException("checkInDate is required but missing in request path")
        val token = checkInService.fetchToken(member.id, checkInDate) ?: throw ResourceNotFoundException("User hasn't check in for date $checkInDate")
        ctx.response()
            .ok(
                jsonObjectOf(
                    "checkInNumber" to token.checkInNumber,
                    "hasEquipment" to token.hasEquipment
                )
            )
    }

    private suspend fun checkInRanking(ctx: RoutingContext) {
        val member = ctx.nerdCaveMember()
        val startDate = ctx.request().params().get("startDate")?.toLocalDate() ?: throw BadRequestException("startDate is required but missing in request path")
        val endDate = ctx.request().params().get("endDate")?.toLocalDate()
        val totalMembers = memberStoreService.totalMembers()
        val allRankings = checkInService.countByMemberId(startDate, endDate)
        val memberRanking = allRankings.find { it.first == member.id }?.let { (_, count) ->
            allRankings.filter { it.second >= count }
                .count()
        }?:totalMembers
        ctx.response()
            .ok(
                jsonObjectOf(
                    "ranking" to memberRanking,
                    "totalMembers" to totalMembers
                )
            )
    }

    private suspend fun currentBranchPeopleNumber(ctx: RoutingContext) {
        val branchId = ctx.request().params().get("branchId")
        val count = checkInService.fetchRecentCheckIns(branchId)
        ctx.response().ok(
            jsonObjectOf(
                "count" to count
            )
        )
    }

}