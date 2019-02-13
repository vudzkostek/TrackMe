package eu.codingtemple.trackme

import java.util.*

class MockConsentStorage : ConsentStorage {

    private val consents = HashMap<Hashable, Boolean>()

    override fun setConsent(sinkId: Hashable, consent: Boolean) {
        consents[sinkId] = consent
    }

    override fun getConsent(sinkId: Hashable): Boolean {
        return if (consents.containsKey(sinkId)) consents[sinkId] ?: false else false
    }
}
