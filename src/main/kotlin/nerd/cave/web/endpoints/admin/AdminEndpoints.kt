package nerd.cave.web.endpoints.admin

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.core.http.Cookie
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.model.admin.*
import nerd.cave.model.api.branch.Branch
import nerd.cave.model.api.branch.LocationInfo
import nerd.cave.model.api.branch.OpenHourInfo
import nerd.cave.service.order.OrderService
import nerd.cave.store.StoreService
import nerd.cave.util.LOCALTIME_FORMMATER
import nerd.cave.util.MongoIdGenerator
import nerd.cave.util.toLocalDateTime
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.exceptions.ResourceNotFoundException
import nerd.cave.web.exceptions.UnauthorizedException
import nerd.cave.web.extentions.coroutine
import nerd.cave.web.extentions.endIfOpen
import nerd.cave.web.extentions.getMandatoryString
import nerd.cave.web.extentions.ok
import nerd.cave.web.session.AdminAccountSessionHandler
import nerd.cave.web.session.adminAccount
import nerd.cave.web.wx.toMD5
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.LocalTime

class AdminEndpoints(
    vertx: Vertx,
    adminSessionHandler: AdminAccountSessionHandler,
    private val clock: Clock,
    storeService: StoreService,
    private val orderService: OrderService
) : HttpEndpoint {

    companion object {
        private val logger = LoggerFactory.getLogger(AdminEndpoints::class.java)
    }

    private val idGenerator = MongoIdGenerator()
    private val adminAccountStoreService by lazy { storeService.adminAccountStoreService }
    private val adminSessionStoreService by lazy { storeService.adminSessionStoreService }
    private val branchStoreService by lazy { storeService.branchStoreService }

    override val router = Router.router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        post("/login") { adminLogin(it) }
        route(adminSessionHandler.handler)
        get("/orders") { retrieveOfflineOrders(it) }
        get("/orders/:startTime") { retrieveOfflineOrders(it) }
        get("/orders/:startTime/:endTime") { retrieveOfflineOrders(it) }
        post("/offlineorder/approve") { approveOfflineOrder(it) }
        post("/branch/") { newBranch(it) }
        post("/branch/deactivate/:id") { deactivateBranch(it) }
    }

    private suspend fun adminLogin(ctx: RoutingContext) {
        val username = ctx.bodyAsJson.getMandatoryString("username")
        val password = ctx.bodyAsJson.getMandatoryString("password")
        val hashedPassword = password.toMD5().toUpperCase()
        val account = adminAccountStoreService.findByUsernamePwd(username, hashedPassword)
            ?: throw UnauthorizedException("User not found or password not matched")
        if (!account.active) throw UnauthorizedException("Account [${username}] is disabled")
        val session = adminSessionStoreService.newSession(account.id, VALID_TIME_SECONDS)
        adminSessionStoreService.disableSessions(session.id)
        ctx.addCookie(Cookie.cookie(ADMIN_SESSION_COOKIE_NAME, session.id).setMaxAge(VALID_TIME_SECONDS))
            .response()
            .ok(jsonObjectOf(
                "nickname" to account.nickname,
                "role" to account.role
            )
            )
    }

    private suspend fun retrieveOfflineOrders(ctx: RoutingContext) {
        val startTime = ctx.request().params().get("startTime")?.toLocalDateTime()
        val endTime = ctx.request().params().get("end")?.toLocalDateTime()
        val enrichedOrders = orderService.orders(startTime, endTime)
        ctx.response().ok(
            jsonArrayOf(
                enrichedOrders.toTypedArray()
            )
        )
    }

    private suspend fun approveOfflineOrder(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        val orderId = ctx.bodyAsJson.getMandatoryString("orderId")
        orderService.approveOfflineOrder(orderId, account)
        ctx.response().ok(
            jsonObjectOf(
                "ok" to 1
            )
        )
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

    private suspend fun deactivateBranch(ctx: RoutingContext) {
        val branchId = ctx.request().params().get("id")
        val deactivateResult = branchStoreService.deactivate(branchId)
        if(!deactivateResult) throw ResourceNotFoundException("Branch id: [$branchId] not found")
        ctx.response().ok(
            jsonObjectOf(
                "ok" to 1
            )
        )
    }

    private fun JsonObject.toBranch(): Branch {
        val id = idGenerator.nextId()
        val name = getMandatoryString("name")
        val locationInfo = getJsonObject("location").toLocationInfo()
        val weekdayOpenHourInfo = getJsonObject("weekdayOpenHour").toOpenHourInfo()
        val holidayOpenHourInfo = getJsonObject("holidayOpenHour").toOpenHourInfo()
        val contactNumbers = getJsonArray("contactNumbers").map { it.toString() }
        val description = getMandatoryString("description")
        return Branch(id, name, locationInfo, weekdayOpenHourInfo, holidayOpenHourInfo, contactNumbers, description, true)
    }

    private fun JsonObject.toLocationInfo(): LocationInfo {
        val longitude = getDouble("longitude")
        val latitude = getDouble("latitude")
        val description = getString("description")
        return LocationInfo(longitude, latitude, description)
    }

    private fun JsonObject.toOpenHourInfo(): OpenHourInfo {
        val openTime = LocalTime.parse(getString("openTime"), LOCALTIME_FORMMATER)
        val closeTime = LocalTime.parse(getString("closeTime"), LOCALTIME_FORMMATER)
        val description = getString("description")
        return OpenHourInfo(openTime, closeTime, description)
    }
}