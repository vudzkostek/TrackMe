package eu.codingtemple.trackme.event

import eu.codingtemple.trackme.event.TargetEvent
import eu.codingtemple.trackme.sink.StringSinkId
import org.assertj.core.api.Assertions
import org.junit.Test

class TargetEventTest {
    @Test
    fun builderCorrect() {
        // given
        val eventId = "eventId"
        val sinkId = StringSinkId("sinkId")
        val key = "key1"
        val value = "value1"

        // when
        val event = TargetEvent.Builder(eventId).attribute(key, value).sink(sinkId).build()

        // then
        Assertions.assertThat(event.eventId).isEqualTo(eventId)
        Assertions.assertThat(event.getAttributes()[key]).isEqualTo(value)
        Assertions.assertThat(event.getTargetSinks()).hasSize(1)
        Assertions.assertThat(event.getTargetSinks()[0].string()).isEqualTo(sinkId.string())
    }
}
