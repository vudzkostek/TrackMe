package eu.codingtemple.trackme

interface ConsentStorage {
    fun setConsent(sinkId: String, consent: Boolean)

    fun getConsent(sinkId: String): Boolean
}
