package nerd.cave.model.admin

import java.time.ZonedDateTime

data class Account(
    val id: String,
    val username: String,
    val password: String,
    val nickname: String,
    val role: Role,
    val active: Boolean,
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

enum class Right{
    CREATE_ACCOUNT,
    APPROVE_OFFLINE_ORDER
}

fun Account.hasRight(right: Right):Boolean {
    return role.hasRight(right)
}