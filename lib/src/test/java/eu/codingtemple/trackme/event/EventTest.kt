package eu.codingtemple.trackme.event

import eu.codingtemple.trackme.event.Event
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


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
        assertThat(event.eventId).isEqualTo(eventId)
        assertThat(event.getAttributes()[key]).isEqualTo(value)
    }
}
