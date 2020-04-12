package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.api.member.Member
import nerd.cave.model.api.member.MemberContact
import nerd.cave.model.api.member.MemberDetail

interface MemberStoreService: LifeCycle {
    suspend fun getOrCreateWechatMember(openid: String, nickName:String, gender: Int): Member
    suspend fun fetchById(id: String): Member?
    suspend fun fetchByIds(ids: List<String>): List<Member>
    suspend fun fetchMembers(start: Int, count: Int): List<Member>
    suspend fun fecthAllMembers(): List<Member>
    suspend fun spendMemberEntry(id: String): Boolean
    suspend fun updateMemberDetail(id: String, memberDetail: MemberDetail)
    suspend fun updateMemberInfo(id: String, memberContact: MemberContact, memberDetail: MemberDetail): Boolean
    suspend fun totalMembers(): Long
    suspend fun updateMemberContact(id: String, memberContact: MemberContact)
}