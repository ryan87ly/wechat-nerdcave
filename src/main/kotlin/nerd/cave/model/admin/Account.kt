package nerd.cave.model.admin

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
        Right.APPROVE_OFFLINE_ORDER
    );

    fun hasRight(right: Right): Boolean {
        return rights.contains(right)
    }
}

enum class AccountStatus {
    ACTIVE,
    DISABLED
}

enum class Right{
    EDIT_ADMIN_ACCOUNT,
    UPDATE_MEMBER_INFO,
    APPROVE_OFFLINE_ORDER
}

fun Account.hasRight(right: Right):Boolean {
    return role.hasRight(right)
}