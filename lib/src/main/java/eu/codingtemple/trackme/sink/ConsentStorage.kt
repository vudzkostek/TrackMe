package eu.codingtemple.trackme.sink

interface ConsentStorage {
    fun setConsent(sinkId: Hashable, consent: Boolean)

    fun getConsent(sinkId: Hashable): Boolean
}
