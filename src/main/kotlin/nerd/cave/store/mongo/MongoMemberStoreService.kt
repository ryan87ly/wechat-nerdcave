package nerd.cave.store.mongo

import nerd.cave.model.member.*
import nerd.cave.store.MemberStoreService
import nerd.cave.util.MongoIdGenerator
import org.litote.kmongo.eq
import java.time.Clock
import java.time.ZonedDateTime

class MongoMemberStoreService(private val clock: Clock, mongoStoreService: MongoStoreService): MemberStoreService {
    private val collection by lazy { mongoStoreService.getCollection<Member>() }
    private val idGenerator by lazy { MongoIdGenerator() }

    override suspend fun getOrCreateWechatMember(openid: String, nickName: String, gender: String): Member {
        val query = "memberSource.openid" eq openid
        val existingMember = collection.findOne(query)
        return if (existingMember != null) {
            existingMember
        } else {
            val member = Member(
                idGenerator.nextId(),
                MemberSourceType.WECHAT,
                WechatMember(openid, nickName, gender),
                ZonedDateTime.now(clock)
            )
            collection.insertOne(member)
            member
        }
    }

    override suspend fun findMember(memberId: String): Member? {
        return collection.findOne(Member::id eq memberId)
    }


}