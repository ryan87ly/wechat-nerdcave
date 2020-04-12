package nerd.cave.model.admin

import nerd.cave.web.exceptions.ForbiddenException
import java.time.ZonedDateTime

data class Account(
    val id: String,
    val username: String,
    val password: String,
    val nickname: String,
    val role: Role,
    val status: AccountStatus,
    val creationTime: ZonedDateTime
)

enum class Role (private vararg val rights: Right) {
    SUPER_USER(
        *Right.values()
    ),
    BRANCH_MAINTAINER(
        Right.MANAGE_OFFLINE_ORDER
    );

    fun hasRight(right: Right): Boolean {
        return rights.contains(right)
    }
}

enum class AccountStatus {
    ACTIVE,
    DISABLED
}

enum class Right(val description: String){
    // Account
    MANAGE_ADMIN_ACCOUNT("Manage admin account"),

    // Member
    MANAGE_MEMBER_ACCOUNT("Manage member account"),

    // Order
    MANAGE_OFFLINE_ORDER("Manage offline order"),
    DOWNLOAD_ORDER("Download order"),

    // Branch
    MANAGE_BRANCH_INFO("Manage branch info"),

    // Product
    MANAGE_PRODUCT_INFO("Manage product info"),

    // Holiday
    MANAGE_HOLIDAY_INFO("Manage holiday info"),

    // Check-in
    MANAGE_CHECKIN_INFO("Manage checkin info"),

    // Notification
    MANAGE_NOTIFICATION("Manage notification")
}

fun Account.hasRight(right: Right):Boolean {
    return role.hasRight(right)
}

fun Account.ensureRight(right: Right) {
    if(!this.hasRight(right)) throw ForbiddenException("Current account is not entitled to ${right.description}")
}