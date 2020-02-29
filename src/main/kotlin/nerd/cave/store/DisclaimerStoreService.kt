package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.disclaimer.DisclaimerSignature

interface DisclaimerStoreService: LifeCycle {
    suspend fun hasSignedDisclaimer(memberId: String): Boolean
    suspend fun signDisclaimer(disclaimer: DisclaimerSignature)
}