package eu.codingtemple.trackme

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

class TrackMeTest {

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    // Test cases:
    // Initialize TrackMe with params
    // Initialize in threads (with listener), init completed listener
    // Initialize blocking (with listener), init completed listener
    // Set consent state to true (from false), listener
    // Set consent state to false (from true), listener
    // set consent state true -> true and false -> false
    // log event to all, blocking
    // log event to all, in threads
    // log event to one
    // silent crashing in init/start/finish
    // silent crashing in logging
    // overriding all consents to true
    // overriding all consents to false
    // finish all sinks


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
