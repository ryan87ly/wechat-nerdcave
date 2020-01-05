package nerd.cave.store.mongo

import nerd.cave.model.member.*
import nerd.cave.store.MemberStoreService
import org.litote.kmongo.eq
import java.time.Clock
import java.time.ZonedDateTime

class MongoMemberStoreService(private val clock: Clock, mongoStoreService: MongoStoreService): MemberStoreService {
    private val collection by lazy { mongoStoreService.getCollection<Member>() }

    override suspend fun getOrCreateWechatMember(openid: String): Member {
        val query = "memberSource.openid" eq openid
        val existingMember = collection.findOne(query)
        if (existingMember != null) {
            return existingMember
        } else {
            val member = Member(
                openid,
                MemberType.NORMAL,
                NormalMember(),
                MemberSourceType.WECHAT,
                WechatMember(openid),
                ZonedDateTime.now(clock)
            )
            collection.insertOne(member)
            return member
        }
    }

    override suspend fun findMember(memberId: String): Member? {
        return collection.findOne(Member::memberId eq memberId)
    }


}