package eu.codingtemple.trackme

class TargetEvent(eventId: String) : Event(eventId) {
    private val targetSinks = mutableListOf<String>()

    fun getTargetSinks() = targetSinks.toList()

    class Builder(eventId: String) {
        private val event: TargetEvent = TargetEvent(eventId)
        fun attribute(key: String, value: String) = apply { event.put(key, value) }
        fun sink(sink: String) = apply { event.targetSinks.add(sink) }
        fun build() = event
    }
}
