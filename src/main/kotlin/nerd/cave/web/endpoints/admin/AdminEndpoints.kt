package nerd.cave.web.endpoints.admin

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.core.http.Cookie
import io.vertx.core.json.JsonObject
import io.vertx.core.json.JsonObject.mapFrom
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.model.admin.*
import nerd.cave.model.api.branch.Branch
import nerd.cave.model.api.branch.LocationInfo
import nerd.cave.model.api.branch.OpenHourInfo
import nerd.cave.model.api.branch.OpenStatus
import nerd.cave.model.api.member.*
import nerd.cave.model.api.notification.Notification
import nerd.cave.model.api.order.OrderCSVDownloader
import nerd.cave.model.api.product.*
import nerd.cave.model.api.token.TokenCSVDownloader
import nerd.cave.service.branch.BranchService
import nerd.cave.service.checkin.CheckInService
import nerd.cave.service.member.MemberService
import nerd.cave.service.order.OrderService
import nerd.cave.store.StoreService
import nerd.cave.util.*
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.exceptions.BadRequestException
import nerd.cave.web.exceptions.ResourceNotFoundException
import nerd.cave.web.exceptions.UnauthorizedException
import nerd.cave.web.extentions.*
import nerd.cave.web.session.AdminAccountSessionHandler
import nerd.cave.web.session.adminAccount
import nerd.cave.web.wx.toMD5
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.LocalTime
import java.time.ZonedDateTime

class AdminEndpoints(
    vertx: Vertx,
    adminSessionHandler: AdminAccountSessionHandler,
    private val clock: Clock,
    storeService: StoreService,
    private val orderService: OrderService,
    private val memberService: MemberService,
    private val branchService: BranchService,
    private val checkInService: CheckInService
) : HttpEndpoint {

    companion object {
        private val logger = LoggerFactory.getLogger(AdminEndpoints::class.java)
    }

    private val idGenerator = MongoIdGenerator()
    private val adminAccountStoreService by lazy { storeService.adminAccountStoreService }
    private val adminSessionStoreService by lazy { storeService.adminSessionStoreService }
    private val branchStoreService by lazy { storeService.branchStoreService }
    private val productStoreService by lazy { storeService.productStoreService }
    private val holidayStoreService by lazy { storeService.publicHolidayStoreService }
    private val notificationStoreService by lazy { storeService.notificationStoreService }

    override val router = Router.router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        options().handler { ctx ->
            ctx.response().apply {
                headers().apply {
                    set("Access-Control-Allow-Origin", "https://nerdcave.club")
                    set("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
                }
                statusCode = HttpResponseStatus.NO_CONTENT.code()
                end()
            }
        }
        post("/login") { adminLogin(it) }
        route(adminSessionHandler.handler)
        get("/accounts") { allAdminAccounts(it) }
        post("/account/") { createAdminAccount(it) }
        post("/account/updatepassword") { updateAdminAccountPassword(it) }
        post("/account/updaterole") { updateAdminAccountRole(it) }
        post("/account/updatestatus") { updateAdminAccountStatus(it) }
        get("/account/info") { getAccountInfo(it) }
        get("/members") { retrieveMembers(it) }
        get("/members/download") { downloadMembers(it) }
        put("/member/:id") { updateMember(it) }
        get("/orders") { retrieveOrders(it) }
        get("/orders/download") { downloadOrders(it) }
        get("/orders/:startTime") { retrieveOrders(it) }
        get("/orders/:startTime/:endTime") { retrieveOrders(it) }
        post("/offlineorder/approve") { approveOfflineOrder(it) }
        get("/branches/") { allBranches(it) }
        post("/branch/") { newBranch(it) }
        put("/branch/:id") { updateBranch(it) }
        get("/branch/manualstatus/:id/:date") { fetchBranchManualStatus(it) }
        post("/branch/manualstatus/:id/:date") { updateBranchManualStatus(it) }
        post("/branch/deactivate/:id") { deactivateBranch(it) }
        get("/products/") { allProducts(it) }
        post("/product/") { newProduct(it) }
        put("/product/:id") { updateProduct(it) }
        get("/holiday") { holidaysForYear(it) }
        post("/holiday/") { addHoliday(it) }
        delete("/holiday/:date") { deleteHoliday(it) }
        get("/checkIn/histories/download") { downloadCheckInHistory(it) }
        get("/checkIn/histories/:startDate") { membersCheckInHistory(it) }
        get("/checkIn/histories/:startDate/:endDate") { membersCheckInHistory(it) }
        post("/notification") { addNotification(it) }

    }

    private suspend fun adminLogin(ctx: RoutingContext) {
        val username = ctx.bodyAsJson.getMandatoryString("username")
        val password = ctx.bodyAsJson.getMandatoryString("password")
        val hashedPassword = password.toMD5().toUpperCase()
        val account = adminAccountStoreService.findByUsernamePwd(username, hashedPassword)
            ?: throw UnauthorizedException("User not found or password not matched")
        if (account.status != AccountStatus.ACTIVE) throw UnauthorizedException("Account [${username}] is disabled")
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

    private suspend fun allAdminAccounts(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_ADMIN_ACCOUNT)
        val accounts = adminAccountStoreService.allAccounts()
        ctx.response().ok(
            jsonArrayOf(
                *accounts.map { it.toJsonObject() }.toTypedArray()
            )
        )
    }

    private suspend fun getAccountInfo(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        ctx.response().ok(
            account.toJsonObject()
        )
    }

    private fun Account.toJsonObject(): JsonObject {
        return jsonObjectOf (
            "id" to id,
            "username" to username,
            "nickname" to nickname,
            "role" to role,
            "status" to status,
            "creationTime" to creationTime.withZoneSameInstant(clock.zone).format(LOCALDATETIME_FORMMATER)
        )
    }

    private suspend fun createAdminAccount(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_ADMIN_ACCOUNT)
        val username = ctx.bodyAsJson.getMandatoryString("username")
        if (adminAccountStoreService.usernameExists(username)) throw BadRequestException("Username $username is already used")
        val newAccount = ctx.bodyAsJson.toNewAdminAccount()
        adminAccountStoreService.newAccount(newAccount)
        ctx.response().endIfOpen(
            HttpResponseStatus.CREATED,
            jsonObjectOf(
                "id" to newAccount.id
            ).encodePrettily()
        )
    }

    private fun JsonObject.toNewAdminAccount(): Account {
        val id = idGenerator.nextId()
        val roleStr = getMandatoryString("role")
        val role = fromString<Role>(roleStr) ?: throw BadRequestException("Unsupported role [$roleStr]")
        return Account(
            id,
            getMandatoryString("username"),
            getMandatoryString("password").toMD5().toUpperCase(),
            getMandatoryString("nickName"),
            role,
            AccountStatus.ACTIVE,
            ZonedDateTime.now(clock)
        )
    }

    private suspend fun updateAdminAccountPassword(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_ADMIN_ACCOUNT)
        val targetId = ctx.bodyAsJson.getString("id") ?: account.id
        if (account.id != targetId && !account.hasRight(Right.MANAGE_ADMIN_ACCOUNT)) throw BadRequestException("Can only update self's password")
        if (adminAccountStoreService.findById(targetId) == null)  throw BadRequestException("Account not found [$targetId]")
        val hashedNewPassword = ctx.bodyAsJson.getMandatoryString("newPassword").toMD5().toUpperCase()
        if (!adminAccountStoreService.updatePassword(targetId, hashedNewPassword)) throw BadRequestException("Update password failed")
        ctx.response().ok(
            jsonObjectOf(
                "ok" to true
            )
        )
    }

    private suspend fun updateAdminAccountRole(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_ADMIN_ACCOUNT)
        val targetId = ctx.bodyAsJson.getMandatoryString("id")
        if (adminAccountStoreService.findById(targetId) == null)  throw BadRequestException("Account not found [$targetId]")
        val roleStr = ctx.bodyAsJson.getMandatoryString("role")
        val role = fromString<Role>(roleStr) ?: throw BadRequestException("Unsupported role [$roleStr]")
        if (!adminAccountStoreService.updateRole(targetId, role)) throw BadRequestException("Update role failed")
        ctx.response().ok(
            jsonObjectOf(
                "ok" to true
            )
        )
    }

    private suspend fun updateAdminAccountStatus(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_ADMIN_ACCOUNT)
        val targetId = ctx.bodyAsJson.getMandatoryString("id")
        if (adminAccountStoreService.findById(targetId) == null)  throw BadRequestException("Account not found [$targetId]")
        val statusStr = ctx.bodyAsJson.getMandatoryString("status")
        val status = fromString<AccountStatus>(statusStr) ?: throw BadRequestException("Supported status [$statusStr]")
        if (!adminAccountStoreService.updateStatus(targetId, status)) throw BadRequestException("Update status failed")
        ctx.response().ok(
            jsonObjectOf(
                "ok" to true
            )
        )
    }

    private suspend fun retrieveMembers(ctx: RoutingContext) {
        val start = ctx.queryParams().get("start").toInt()
        val count = ctx.queryParams().get("count").toInt()
        val members = memberService.getRawMembersInfo(start, count)
        ctx.response().ok(
            jsonArrayOf(
                *members.map { it.toJson() }.toTypedArray()
            )
        )
    }

    private suspend fun downloadMembers(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_MEMBER_ACCOUNT)
        val members = memberService.getAllRawMembersInfo()
        val content = MemberCSVDownloader(members).toCSVString()
        val fileName = "${ZonedDateTime.now(clock).toFormattedString()}-Members.csv"
        ctx.response().respondCSV(content, fileName)
    }

    private suspend fun updateMember(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_MEMBER_ACCOUNT)
        val id = ctx.request().params().get("id")
        val legalName = ctx.bodyAsJson.getMandatoryString("legalName")
        val contactNumber = ctx.bodyAsJson.getMandatoryString("contactNumber")
        val emergentContactNumber = ctx.bodyAsJson.getMandatoryString("emergentContactNumber")
        val memberDetail = ctx.bodyAsJson.getMandatoryJsonObject("memberDetail").toMemberDetail()
        if (!memberService.updateMemberInfo(id, MemberContact(legalName, contactNumber, emergentContactNumber), memberDetail)) throw BadRequestException("No member [$id] found")
        ctx.response().ok(
            jsonObjectOf(
                "ok" to true
            )
        )
    }

    private fun Member.toJson(): JsonObject {
        return jsonObjectOf(
            "id" to this.id,
            "wechatNickName" to when(val memberSource = this.memberSource) {
                is WechatMember -> memberSource.nickName
                else -> null
            },
            "legalName" to this.memberContact?.legalName,
            "contactNumber" to this.memberContact?.contactNumber,
            "emergentContactNumber" to this.memberContact?.emergentContactNumber,
            "memberDetail" to this.memberDetail
        )
    }

    private fun JsonObject.toMemberDetail(): MemberDetail {
        val memberTypeStr = getString("memberType")
        val memberType = fromString<MemberType>(memberTypeStr) ?: throw BadRequestException("Unsupported member type $memberTypeStr")
        return when(memberType) {
            MemberType.MULTI_ENTRIES -> {
                MultiEntriesMember(
                    getMandatoryInt("usedEntries", "memberDetail"),
                    getMandatoryInt("totalEntries", "memberDetail"),
                    getMandatoryString("validFrom", "memberDetail").toLocalDate(),
                    getMandatoryString("validUntil", "memberDetail").toLocalDate()
                )
            }
            MemberType.MONTHLY -> {
                MonthlyMember(
                    getMandatoryString("validFrom", "memberDetail").toLocalDate(),
                    getMandatoryString("validUntil", "memberDetail").toLocalDate()
                )
            }
            MemberType.YEARLY -> {
                YearlyMember(
                    getMandatoryString("validFrom", "memberDetail").toLocalDate(),
                    getMandatoryString("validUntil", "memberDetail").toLocalDate()
                )
            }
            MemberType.NORMAL -> {
                NormalMember(0)
            }
        }
    }

    private suspend fun retrieveOrders(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_OFFLINE_ORDER)
        val startTime = ctx.request().params().get("startTime")?.toLocalDateTime()
        val endTime = ctx.request().params().get("endTime")?.toLocalDateTime()
        val enrichedOrders = orderService.orders(startTime, endTime)
        ctx.response().ok(
            jsonArrayOf(
                *enrichedOrders.toTypedArray()
            )
        )
    }

    private suspend fun downloadOrders(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.DOWNLOAD_ORDER)
        val enrichedOrders = orderService.allOrders()
        val content = OrderCSVDownloader(enrichedOrders)
            .toCSVString()
        val fileName = "${ZonedDateTime.now(clock).toFormattedString()}-Orders.csv"
        ctx.response().respondCSV(content, fileName)

    }

    private suspend fun approveOfflineOrder(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_OFFLINE_ORDER)
        val orderId = ctx.bodyAsJson.getMandatoryString("orderId")
        orderService.approveOfflineOrder(orderId, account)
        ctx.response().ok(
            jsonObjectOf(
                "ok" to true
            )
        )
    }

    private suspend fun allBranches(ctx: RoutingContext) {
        val allBranches = branchStoreService.fetchAll()
        ctx.response()
            .ok(
                jsonArrayOf(
                    *allBranches.map { mapFrom(it) }.toTypedArray()
                )
            )
    }

    private suspend fun newBranch(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_BRANCH_INFO)
        val requestBody = ctx.bodyAsJson
        val branchId = idGenerator.nextId()
        val branch = requestBody.toBranch(branchId)
        branchStoreService.createBranch(branch)
        ctx.response()
            .endIfOpen(
                HttpResponseStatus.CREATED,
                jsonObjectOf(
                    "id" to branch.id
                ).encodePrettily()
            )
    }

    private suspend fun updateBranch(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_BRANCH_INFO)
        val requestBody = ctx.bodyAsJson
        val branchId = ctx.request().params().get("id")
        val branch = requestBody.toBranch(branchId)
        if (!branchStoreService.updateBranch(branch)) {
            throw BadRequestException("No branch with id [$branchId] found")
        }
        ctx.response()
            .ok(
                jsonObjectOf(
                    "ok" to true
                )
            )
    }

    private suspend fun fetchBranchManualStatus(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_BRANCH_INFO)
        val branchId = ctx.request().params().get("id")
        val date = ctx.request().params().get("date").toLocalDate()
        val branchOpenStatus = branchService.fetchBranchOpenStatus(branchId, date)
        val responseBranchOpenStatus = branchOpenStatus?.status?.name ?: "UNSET"
        ctx.response().ok(
            jsonObjectOf(
                "branchId" to branchId,
                "date" to date.toFormattedString(),
                "status" to responseBranchOpenStatus
            )
        )
    }

    private suspend fun updateBranchManualStatus(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_BRANCH_INFO)
        val branchId = ctx.request().params().get("id")
        val date = ctx.request().params().get("date").toLocalDate()
        val statusStr = ctx.bodyAsJson.getMandatoryString("status")
        val status = fromString<OpenStatus>(statusStr) ?: throw BadRequestException("Unsupported status $statusStr")
        if (!branchService.updateBranchOpenStatus(branchId, date, status)) throw BadRequestException("Update branch manual status failed")
        ctx.response().ok(
            jsonObjectOf(
                "ok" to true
            )
        )
    }

    private suspend fun deactivateBranch(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_BRANCH_INFO)
        val branchId = ctx.request().params().get("id")
        val deactivateResult = branchStoreService.deactivate(branchId)
        if(!deactivateResult) throw ResourceNotFoundException("Branch id: [$branchId] not found")
        ctx.response().ok(
            jsonObjectOf(
                "ok" to true
            )
        )
    }

    private fun JsonObject.toBranch(id: String): Branch {
        val name = getMandatoryString("name")
        val locationInfo = getJsonObject("location").toLocationInfo()
        val weekdayOpenHourInfo = getJsonObject("weekdayOpenHour").toOpenHourInfo()
        val holidayOpenHourInfo = getJsonObject("holidayOpenHour").toOpenHourInfo()
        val contactNumbers = getJsonArray("contactNumbers").map { it.toString() }
        val description = getMandatoryString("description")
        val active = getMandatoryBoolean("active")
        return Branch(id, name, locationInfo, weekdayOpenHourInfo, holidayOpenHourInfo, contactNumbers, description, active)
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

    private suspend fun allProducts(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_PRODUCT_INFO)
        val products = productStoreService.fetchAll()
        ctx.response()
            .ok(
                jsonArrayOf(
                    *products.map { mapFrom(it) }.toTypedArray()
                )
            )
    }

    private suspend fun newProduct(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_PRODUCT_INFO)
        val productId = idGenerator.nextId()
        val product = ctx.bodyAsJson.toProduct(productId)
        productStoreService.createProduct(product)
        ctx.response()
            .endIfOpen(
                HttpResponseStatus.CREATED,
                jsonObjectOf(
                    "id" to product.id
                ).encodePrettily()
            )
    }

    private suspend fun updateProduct(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_PRODUCT_INFO)
        val id = ctx.request().params().get("id")
        val product = ctx.bodyAsJson.toProduct(id)
        if (!productStoreService.updateProduct(product)) throw BadRequestException("No Product with id [$id] found")
        ctx.response()
            .ok(
                jsonObjectOf(
                    "ok" to true
                )
            )
    }

    private fun JsonObject.toProduct(id: String): Product {
        val productTypeStr = getMandatoryString("productType")
        val productType = fromString<ProductType>(productTypeStr) ?: throw BadRequestException("Unsupported product type [$productTypeStr]")
        val detailJson = getJsonObject("detail")
        val detail = detailJson.toProductDetail(productType)
        val name = getMandatoryString("name")
        val description = getMandatoryString("description")
        val regularPrice = getMandatoryInt("regularPrice")
        val discounts = getJsonArray("discounts").map { (it as? JsonObject)?.toProductDiscount() ?: throw BadRequestException("Discount should be Json object") }
        val payViaWechat = getBoolean("payViaWechat")
        val enabled = getBoolean("enabled")
        return Product(id, name, description, productType, detail, discounts, regularPrice, payViaWechat, enabled)
    }

    private fun JsonObject.toProductDetail(productType: ProductType): ProductDetail {
        return when(productType) {
            ProductType.MULTI_ENTRIES_FEE -> MultiEntriesProduct(getMandatoryInt("entries"), getMandatoryInt("validMonths"))
            ProductType.MONTHLY_MEMBER_FEE -> MonthlyMemberProduct(getMandatoryInt("months"))
            ProductType.YEARLY_MEMBER_FEE -> YearlyMemberProduct(getMandatoryInt("years"))
            ProductType.EQUIPMENT_RENTAL_FEE -> EquipmentRentalProduct()
            ProductType.SINGLE_ENTRY_FEE -> SingleEntryProduct()
        }
    }

    private fun JsonObject.toProductDiscount(): ProductDiscount {
        val typeStr = getMandatoryString("discountType")
        val type = fromString<DiscountType>(typeStr) ?: throw BadRequestException("Unsupported discount type [$typeStr]")
        val description = getMandatoryString("description")
        val detailObj = getJsonObject("detail")
        val detail = detailObj.toDiscountDetail(type)
        return ProductDiscount(type, description, detail)
    }

    private fun JsonObject.toDiscountDetail(type: DiscountType): DiscountDetail {
        return when(type) {
            DiscountType.WEEK_DAY -> WeekDayDiscount(getMandatoryInt("salePrice"))
        }
    }

    private suspend fun holidaysForYear(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_HOLIDAY_INFO)
        val year = ctx.queryParams().get("year").toInt()
        val allHolidays = holidayStoreService.getHolidays(year)
        ctx.response().ok(
            jsonArrayOf(
                *allHolidays.map { it.date.toFormattedString() }.toTypedArray()
            )
        )
    }

    private suspend fun addHoliday(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_HOLIDAY_INFO)
        val date = ctx.bodyAsJson.getMandatoryString("date").toLocalDate()
        if (holidayStoreService.hasRecord(date)) throw BadRequestException("${date.toFormattedString()} is already set as public holiday")
        holidayStoreService.addHoliday(date)
        ctx.response().endIfOpen(
            HttpResponseStatus.CREATED,
            jsonObjectOf(
                "ok" to true
            ).encodePrettily()
        )
    }

    private suspend fun deleteHoliday(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_HOLIDAY_INFO)
        val date = ctx.request().params().get("date").toLocalDate()
        if (!holidayStoreService.hasRecord(date)) throw BadRequestException("${date.toFormattedString()} is not set as public holiday")
        holidayStoreService.removeHoliday(date)
        ctx.response().ok(
            jsonObjectOf(
                "ok" to true
            )
        )
    }

    private suspend fun downloadCheckInHistory(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_CHECKIN_INFO)
        val enrichedTokens = checkInService.allCheckInHistory()
        val content = TokenCSVDownloader(enrichedTokens)
            .toCSVString(clock)
        val fileName = "${ZonedDateTime.now(clock).toFormattedString()}-CheckInHistory.csv"
        ctx.response().respondCSV(content, fileName)
    }

    private suspend fun membersCheckInHistory(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_CHECKIN_INFO)
        val startDate = ctx.request().params().get("startDate")?.toLocalDate() ?: throw BadRequestException("startDate is required but missing in request path")
        val endDate = ctx.request().params().get("endDate")?.toLocalDate()
        val tokens = checkInService.membersCheckInHistory(startDate, endDate)
        ctx.response().ok(
            jsonArrayOf(
                * tokens.map { mapFrom(it) }.toTypedArray()
            )
        )
    }

    private suspend fun addNotification(ctx: RoutingContext) {
        val account = ctx.adminAccount()
        account.ensureRight(Right.MANAGE_NOTIFICATION)
        val id = idGenerator.nextId()
        val title = ctx.bodyAsJson.getMandatoryString("title")
        val detail = ctx.bodyAsJson.getMandatoryString("detail")
        val now = ZonedDateTime.now(clock)
        val notification = Notification(id, title, detail, now)
        notificationStoreService.insertNotification(notification)
        ctx.response().endIfOpen(
            HttpResponseStatus.CREATED,
            jsonObjectOf(
                "ok" to true
            ).encodePrettily()
        )
    }
}