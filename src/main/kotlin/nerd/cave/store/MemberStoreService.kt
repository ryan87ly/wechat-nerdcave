package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.api.member.Member
import nerd.cave.model.api.member.MemberContact
import nerd.cave.model.api.member.MemberDetail

interface MemberStoreService: LifeCycle {
    suspend fun getOrCreateWechatMember(openid: String, nickName:String, gender: Int): Member
    suspend fun fetchById(id: String): Member?
    suspend fun spendMemberEntry(memberId: String): Boolean
    suspend fun updateMemberDetail(memberId: String, memberDetail: MemberDetail)
    suspend fun totalMembers(): Long
    suspend fun updateMemberContact(memberId: String, memberContact: MemberContact)
}