package nerd.cave.store.mongo

import nerd.cave.disclaimer.DisclaimerSignature
import nerd.cave.store.DisclaimerStoreService
import org.litote.kmongo.eq

class MongoDisclaimerStoreService(storeService: MongoStoreService): DisclaimerStoreService {
    private val collection by lazy { storeService.getCollection<DisclaimerSignature>() }

    override suspend fun hasSignedDisclaimer(memberId: String): Boolean {
        val query = DisclaimerSignature::memberId eq memberId
        return collection.countDocuments(query) > 0
    }

    override suspend fun signDisclaimer(disclaimer: DisclaimerSignature) {
        collection.insertOne(disclaimer)
    }


}