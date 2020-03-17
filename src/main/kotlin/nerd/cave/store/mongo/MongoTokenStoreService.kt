package nerd.cave.store.mongo

import nerd.cave.model.api.token.Token
import nerd.cave.store.TokenStoreService
import nerd.cave.util.toFormattedString
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.*
import java.time.LocalDate

class MongoTokenStoreService(storeService: MongoStoreService): TokenStoreService {
    private val collection by lazy { storeService.getCollection<Token>() }

    override suspend fun insertToken(token: Token) {
        collection.insertOne(token)
    }

    override suspend fun fetchToken(memberId: String, checkInDate: LocalDate): Token? {
        val query = and(
            Token::memberId eq memberId,
            "checkInDate" gte checkInDate.toFormattedString()
        )
        return collection.findOne(query)
    }

    override suspend fun history(memberId: String, startDateInclusive: LocalDate, endDateExclusive: LocalDate?): List<String> {
        val baseQuery = and(
            Token::memberId eq memberId,
            "checkInDate" gte startDateInclusive.toFormattedString()
        )
        val query = if (endDateExclusive == null) baseQuery else and(baseQuery, "checkInDate" lt endDateExclusive.toFormattedString())
        return collection.find(query).toList()
            .map { it.checkInDate }
            .map { it.toFormattedString() }
            .distinct()
            .sorted()
    }

    data class RankingResult(
        @BsonId
        val memberId: String,
        val count: Long
    )

    override suspend fun countByMemberId(startDateInclusive: LocalDate, endDateExclusive: LocalDate?): List<Pair<String, Long>> {
        val baseCondition = "checkInDate" gte startDateInclusive.toFormattedString()
        val condition = if (endDateExclusive == null) baseCondition else and(baseCondition, "checkInDate" lt endDateExclusive.toFormattedString())
        return collection.aggregate<RankingResult>(
            listOf(
                match(condition),
                group(
                    Token::memberId,
                    RankingResult::count sum 1
                ),
                sort(
                    descending(
                        RankingResult::count
                    )
                )
            )
        ).toList().map { it.memberId to it.count }
    }



}