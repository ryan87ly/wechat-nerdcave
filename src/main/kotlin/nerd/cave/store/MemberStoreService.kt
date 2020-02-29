package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.member.Member

interface MemberStoreService: LifeCycle {
    suspend fun getOrCreateWechatMember(openid: String, nickName:String, gender: String): Member
    suspend fun findMember(memberId: String): Member?
}