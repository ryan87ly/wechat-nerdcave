package nerd.cave.store.mongo

import nerd.cave.model.token.Token
import nerd.cave.model.token.toTokenDateFormat
import nerd.cave.store.TokenStoreService
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gte
import org.litote.kmongo.lt
import java.time.LocalDate

class MongoTokenStoreService(storeService: MongoStoreService): TokenStoreService {
    private val collection by lazy { storeService.getCollection<Token>() }

    override suspend fun insertToken(token: Token) {
        collection.insertOne(token)
    }

    override suspend fun fetchToken(memberId: String, checkInDate: LocalDate): Token? {
        val query = and(
            Token::memberId eq memberId,
            Token::checkInDate gte checkInDate.toTokenDateFormat()
        )
        return collection.findOne(query)
    }

    override suspend fun history(memberId: String, startDateInclusive: LocalDate, endDateExclusive: LocalDate?): List<String> {
        val baseQuery = and(
            Token::memberId eq memberId,
            Token::checkInDate gte startDateInclusive.toTokenDateFormat()
        )
        val query = if (endDateExclusive == null) baseQuery else and(baseQuery, Token::checkInDate lt endDateExclusive.toTokenDateFormat())
        return collection.find(query).toList()
            .map { it.checkInDate }
            .distinct()
            .sorted()
    }

}