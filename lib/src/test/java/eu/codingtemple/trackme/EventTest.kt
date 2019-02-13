package eu.codingtemple.trackme

import org.junit.Test

import org.junit.Assert.assertEquals

class EventTest {
    @Test
    fun builderCorrect() {
        // given
        val eventId = "eventId"
        val key = "attr1"
        val value = "value1"

        // when
        val event = Event.Builder(eventId).attribute(key, value).build()

        // then
        assertEquals(eventId, event.eventId)
        assertEquals(value, event.getAttributes()[key])
    }
}
