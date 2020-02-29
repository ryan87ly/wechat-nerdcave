package nerd.cave.web.endpoints.api.branch

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.json.JsonObject.mapFrom
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.model.branch.Branch
import nerd.cave.model.branch.LocationInfo
import nerd.cave.model.branch.OpenHourInfo
import nerd.cave.store.BranchStoreService
import nerd.cave.util.MongoIdGenerator
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.exceptions.ResourceNotFoundException
import nerd.cave.web.extentions.coroutine
import nerd.cave.web.extentions.endIfOpen
import nerd.cave.web.extentions.ok
import nerd.cave.web.session.NerdCaveSessionHandler
import org.slf4j.LoggerFactory
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class BranchEndpoints(vertx: Vertx, private val branchStoreService: BranchStoreService, sessionHandler: NerdCaveSessionHandler): HttpEndpoint {
    private val idGenerator = MongoIdGenerator()

    companion object {
        private val logger = LoggerFactory.getLogger(BranchEndpoints::class.java)
        private val openHourTimeFormatter = DateTimeFormatter.ofPattern("HHmmss")
    }

    override val router: Router = Router.router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        route(sessionHandler.handler)
        post("/") { newBranch(it) }
        get("/all") { allBranches(it) }
        get("/:id") { branchById(it)}
        delete("/:id") { deleteBranchById(it) }
    }

    private suspend fun newBranch(ctx: RoutingContext) {
        val requestBody = ctx.bodyAsJson
        val branch = requestBody.toBranch()
        branchStoreService.createBranch(branch)
        ctx.response()
            .endIfOpen(
                HttpResponseStatus.CREATED,
                jsonObjectOf(
                    "id" to branch.id
                ).encodePrettily()
            )
    }

    private suspend fun allBranches(ctx: RoutingContext) {
        val branches = branchStoreService.fetchAll()
        ctx.response().ok(jsonArrayOf(*branches.toTypedArray()))
    }

    private suspend fun branchById(ctx: RoutingContext) {
        val branchId = ctx.request().params().get("id")
        val branch = branchStoreService.fetchById(branchId) ?: throw ResourceNotFoundException("Branch id: [$branchId] not found")
        ctx.response().ok(mapFrom(branch))
    }

    private suspend fun deleteBranchById(ctx: RoutingContext) {
        val branchId = ctx.request().params().get("id")
        val deleteResult = branchStoreService.deleteById(branchId)
        if(deleteResult) ctx.response().endIfOpen(HttpResponseStatus.NO_CONTENT) else throw ResourceNotFoundException("Branch id: [$branchId] not found")
    }

    private fun JsonObject.toBranch(): Branch {
        val id = idGenerator.nextId()
        val locationInfo = getJsonObject("location").toLocationInfo()
        val openHourInfo = getJsonObject("openHour").toOpenHourInfo()
        val contactNumbers = getJsonArray("contactNumbers").map { it.toString() }
        val description = getString("description")
        return Branch(id, locationInfo, openHourInfo, contactNumbers, description)
    }

    private fun JsonObject.toLocationInfo(): LocationInfo {
        val longitude = getDouble("longitude")
        val latitude = getDouble("latitude")
        val description = getString("description")
        return LocationInfo(longitude, latitude, description)
    }

    private fun JsonObject.toOpenHourInfo(): OpenHourInfo {
        val openTime = LocalTime.parse(getString("openTime"), openHourTimeFormatter)
        val closeTime = LocalTime.parse(getString("closeTime"), openHourTimeFormatter)
        val description = getString("description")
        return OpenHourInfo(openTime, closeTime, description)
    }


}