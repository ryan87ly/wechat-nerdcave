package nerd.cave.web.endpoints.api

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject.mapFrom
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.coroutines.dispatcher
import nerd.cave.service.branch.BranchService
import nerd.cave.web.endpoints.HttpEndpoint
import nerd.cave.web.exceptions.ResourceNotFoundException
import nerd.cave.web.extentions.coroutine
import nerd.cave.web.extentions.ok
import nerd.cave.web.session.NerdCaveSessionHandler
import org.slf4j.LoggerFactory

class BranchEndpoints(
    vertx: Vertx,
    sessionHandler: NerdCaveSessionHandler,
    private val branchService: BranchService
): HttpEndpoint {
    companion object {
        private val logger = LoggerFactory.getLogger(BranchEndpoints::class.java)
    }

    override val router: Router = Router.router(vertx).coroutine(vertx.dispatcher(), logger).apply {
        route(sessionHandler.handler)
        get("/all") { allBranches(it) }
        get("/:id") { branchById(it)}
    }

    private suspend fun allBranches(ctx: RoutingContext) {
        val branches = branchService.allBranchClientInfo()
        ctx.response().ok(
            jsonArrayOf(
                *branches.toTypedArray()
            )
        )
    }

    private suspend fun branchById(ctx: RoutingContext) {
        val branchId = ctx.request().params().get("id")
        val branch = branchService.findById(branchId) ?: throw ResourceNotFoundException("Branch id: [$branchId] not found")
        ctx.response().ok(mapFrom(branch))
    }
}