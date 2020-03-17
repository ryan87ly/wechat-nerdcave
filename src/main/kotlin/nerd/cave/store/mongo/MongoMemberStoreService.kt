package nerd.cave.store.mongo

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Updates
import nerd.cave.model.api.member.Member
import nerd.cave.model.api.member.MemberSourceType
import nerd.cave.model.api.member.WechatMember
import nerd.cave.model.api.member.*
import nerd.cave.store.MemberStoreService
import nerd.cave.util.MongoIdGenerator
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import org.litote.kmongo.where
import java.time.Clock
import java.time.ZonedDateTime

class MongoMemberStoreService(private val clock: Clock, mongoStoreService: MongoStoreService): MemberStoreService {
    private val collection by lazy { mongoStoreService.getCollection<Member>() }
    private val idGenerator by lazy { MongoIdGenerator() }

    override suspend fun start() {
        collection.ensureIndex("id" eq 1, IndexOptions().unique(true))
        collection.ensureIndex("memberSource.openid" eq 1, IndexOptions().unique(true))
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

    override suspend fun spendMemberEntry(memberId: String): Boolean {
        val query = and(
            "id" eq memberId,
            "memberDetail.memberType" eq MemberType.MULTI_ENTRIES.name,
            where("this.memberDetail.usedEntries < this.memberDetail.totalEntries" )
        )
        val update = Updates.inc("memberDetail.usedEntries", 1)
        return collection.updateOne(query, update).succeedUpdateOne()
    }

    override suspend fun updateMemberDetail(memberId: String, memberDetail: MemberDetail) {
        val query = "id" eq memberId
        val update = setValue(Member::memberDetail, memberDetail)
        collection.updateOne(query, update)
    }

    override suspend fun totalMembers(): Long {
        return collection.countDocuments()
    }

    override suspend fun updateMemberContact(memberId: String, memberContact: MemberContact) {
        val query = "id" eq memberId
        val update = setValue(Member::memberContact, memberContact)
        collection.updateOne(query, update)
    }


}