package eu.codingtemple.trackme

import org.junit.Test

import org.junit.Assert.assertEquals

class TargetEventTest {
    @Test
    fun builderCorrect() {
        // given
        val eventId = "eventId"
        val sinkId = "sinkId"
        val key = "key1"
        val value = "value1"

        // when
        val event = TargetEvent.Builder(eventId).attribute(key, value).sink(sinkId).build()

        // then
        assertEquals(eventId, event.eventId)
        assertEquals(value, event.getAttributes()[key])
        assertEquals(1, event.getTargetSinks().size)
        assertEquals(sinkId, event.getTargetSinks()[0])
    }
}
