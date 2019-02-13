package eu.codingtemple.trackme.event

open class Event(val eventId: String) {
    private val attributes = mutableMapOf<String, String>()

    internal fun put(key: String, value: String) {
        attributes[key] = value
    }

    fun getAttributes() = attributes.toMap()

    class Builder(eventId: String) {
        private val event: Event = Event(eventId)
        fun attribute(key: String, value: String) = apply { event.put(key, value) }
        fun build() = event
    }
}
