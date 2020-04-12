package nerd.cave.store.mongo

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Updates
import nerd.cave.model.api.member.Member
import nerd.cave.model.api.member.MemberSourceType
import nerd.cave.model.api.member.WechatMember
import nerd.cave.model.api.member.*
import nerd.cave.store.MemberStoreService
import nerd.cave.util.MongoIdGenerator
import org.litote.kmongo.*
import java.time.Clock
import java.time.ZonedDateTime

class MongoMemberStoreService(private val clock: Clock, mongoStoreService: MongoStoreService): MemberStoreService {
    private val collection by lazy { mongoStoreService.getCollection<Member>() }
    private val idGenerator by lazy { MongoIdGenerator() }

    override suspend fun start() {
        collection.ensureIndex("id" eq 1, IndexOptions().unique(true))
        collection.ensureIndex("memberSource.openid" eq 1, IndexOptions().unique(true))
        collection.ensureIndex("registerTime" eq -1)
    }

    override suspend fun getOrCreateWechatMember(openid: String, nickName: String, gender: Int): Member {
        val query = "memberSource.openid" eq openid
        val existingMember = collection.findOne(query)
        return if (existingMember != null) {
            val updatedMember = existingMember.copy(memberSource = WechatMember(openid, nickName, gender))
            collection.updateOne(query, existingMember)
            updatedMember
        } else {
            val member = Member(
                idGenerator.nextId(),
                MemberSourceType.WECHAT,
                WechatMember(openid, nickName, gender),
                NormalMember(),
                ZonedDateTime.now(clock)
            )
            collection.insertOne(member)
            member
        }
    }

    override suspend fun fetchById(id: String): Member? {
        return collection.findOne(Member::id eq id)
    }

    override suspend fun fetchByIds(ids: List<String>): List<Member> {
        val query = "id" `in` ids
        return collection.find(query).toList()
    }

    override suspend fun fetchMembers(start: Int, count: Int): List<Member> {
        return collection.find()
            .sort("registerTime" eq  -1)
            .skip(start)
            .limit(count)
            .toList()
    }

    override suspend fun fecthAllMembers(): List<Member> {
        return collection.find()
            .sort("registerTime" eq  -1)
            .toList()
    }

    override suspend fun spendMemberEntry(id: String): Boolean {
        val query = and(
            "id" eq id,
            "memberDetail.memberType" eq MemberType.MULTI_ENTRIES.name,
            where("this.memberDetail.usedEntries < this.memberDetail.totalEntries" )
        )
        val update = Updates.inc("memberDetail.usedEntries", 1)
        return collection.updateOne(query, update).succeedUpdateOne()
    }

    override suspend fun updateMemberDetail(id: String, memberDetail: MemberDetail) {
        val query = "id" eq id
        val update = setValue(Member::memberDetail, memberDetail)
        collection.updateOne(query, update)
    }

    override suspend fun updateMemberInfo(id: String, memberContact: MemberContact, memberDetail: MemberDetail): Boolean {
        val query = "id" eq id
        val update = combine(
            setValue(Member::memberContact, memberContact),
            setValue(Member::memberDetail, memberDetail)
        )
        return collection.updateOne(query, update).succeedUpdateOne()
    }

    override suspend fun totalMembers(): Long {
        return collection.countDocuments()
    }

    override suspend fun updateMemberContact(id: String, memberContact: MemberContact) {
        val query = "id" eq id
        val update = setValue(Member::memberContact, memberContact)
        collection.updateOne(query, update)
    }


}