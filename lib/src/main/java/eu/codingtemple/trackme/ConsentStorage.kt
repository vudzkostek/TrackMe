package eu.codingtemple.trackme

interface ConsentStorage {
    fun setConsent(sinkId: Hashable, consent: Boolean)

    fun getConsent(sinkId: Hashable): Boolean
}
