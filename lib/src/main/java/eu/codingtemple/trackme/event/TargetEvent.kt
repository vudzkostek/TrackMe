package eu.codingtemple.trackme.event

import eu.codingtemple.trackme.sink.Hashable

class TargetEvent(eventId: String) : Event(eventId) {
    private val targetSinks = mutableListOf<Hashable>()

    fun getTargetSinks() = targetSinks.toList()

    class Builder(eventId: String) {
        private val event: TargetEvent =
            TargetEvent(eventId)
        fun attribute(key: String, value: String) = apply { event.put(key, value) }
        fun sink(sink: Hashable) = apply { event.targetSinks.add(sink) }
        fun build() = event
    }
}
