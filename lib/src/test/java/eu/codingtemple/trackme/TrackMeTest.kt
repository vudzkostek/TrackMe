package eu.codingtemple.trackme

import org.assertj.core.api.Assertions
import org.junit.Test

class TrackMeTest {

    @Test
    fun builderCorrect() {
        // given
        val eventId = "eventId"
        val key = "attr1"
        val value = "value1"

        // when
        val event = Event.Builder(eventId).attribute(key, value).build()

        // then
        Assertions.assertThat(event.eventId).isEqualTo(eventId)
        Assertions.assertThat(event.getAttributes()[key]).isEqualTo(value)
    }
}
