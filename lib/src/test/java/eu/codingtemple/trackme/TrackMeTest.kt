package eu.codingtemple.trackme

import android.content.Context
import com.nhaarman.mockitokotlin2.*
import eu.codingtemple.trackme.event.Event
import eu.codingtemple.trackme.event.TargetEvent
import eu.codingtemple.trackme.sink.Hashable
import eu.codingtemple.trackme.sink.Sink
import eu.codingtemple.trackme.sink.StringSinkId
import io.reactivex.Single
import io.reactivex.functions.Consumer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.concurrent.TimeUnit

class TrackMeTest {

    @get:Rule
    val schedulerRule = TestSchedulerRule()

    @Mock
    lateinit var sinkListener: SinkStateListener

    @Mock
    lateinit var sink1: Sink

    @Mock
    lateinit var sink2: Sink

    @Mock
    lateinit var sink3: Sink

    @Mock
    lateinit var context: Context

    private val sinkIdCaptor = KArgumentCaptor<Hashable>(ArgumentCaptor.forClass(Hashable::class.java), Hashable::class)

    private val sink1Id = StringSinkId("sink1Id")
    private val sink2Id = StringSinkId("sink2Id")

    private val initSuccessConsumer: Consumer<Pair<Hashable, Boolean>> = Consumer { sink3.initialize(context) }
    private val startSuccessConsumer: Consumer<Pair<Hashable, Boolean>> = Consumer { sink3.start() }
    private val finishSuccessConsumer: Consumer<Pair<Hashable, Boolean>> = Consumer { sink3.finish() }

    private lateinit var trackMeSync: TrackMe
    private lateinit var trackMeAsync: TrackMe

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        given(sink1.id).willReturn(sink1Id)
        given(sink2.id).willReturn(sink2Id)

        trackMeSync = TrackMe.Builder()
            .withBlocking(true)
            .withSinkListener(sinkListener)
            .withSink(sink1)
            .withSink(sink2)
            .build()

        trackMeAsync = TrackMe.Builder()
            .withSinkListener(sinkListener)
            .withSink(sink1)
            .withSink(sink2)
            .build()
    }

    // Test cases:

    // Build TrackMe with params

    @Test
    fun shouldBuildTrackMeWithAllParams() {
        // given
        val blocking = true
        val silentCrashing = false
        val consentOverride = true
        val overrideValue = true

        // when
        val trackMeBuilt = TrackMe.Builder()
            .withBlocking(blocking)
            .withSinkListener(sinkListener)
            .withSilentCrashing(silentCrashing)
            .withConsentOverride(consentOverride, overrideValue)
            .withSink(sink1)
            .withSink(sink2)
            .build()

        // then
        assertThat(trackMeBuilt.blocking).isEqualTo(blocking)
        assertThat(trackMeBuilt.silentCrashing).isEqualTo(silentCrashing)
        assertThat(trackMeBuilt.overrideConsent).isEqualTo(consentOverride)
        assertThat(trackMeBuilt.overrideValue).isEqualTo(overrideValue)
        assertThat(trackMeBuilt.sinks).hasSize(2)
        assertThat(trackMeBuilt.sinkListener).isEqualTo(sinkListener)
    }

    // Initialize in threads (with listener), init completed listener

    @Test
    fun shouldInitializeTrackMeAsync() {
        // given
        given(sink1.consent).willReturn(true)
        given(sink2.consent).willReturn(true)

        whenever(sink1.initialize(any())).then { delayedSingle(5, sink1.id, initSuccessConsumer) }
        whenever(sink2.initialize(any())).then { delayedSingle(5, sink2.id, initSuccessConsumer) }

        // when
        trackMeAsync.initialize(context)

        // then
        verify(sink1).initialize(eq(context))
        verify(sink2).initialize(eq(context))
        verifyZeroInteractions(sink3)
        verifyZeroInteractions(sinkListener)

        // when
        schedulerRule.testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // then
        verifyZeroInteractions(sink3)
        verifyZeroInteractions(sinkListener)

        // when
        schedulerRule.testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // then
        verify(sink3, times(2)).initialize(any())
        verify(sinkListener, times(2)).onSinkInitialized(sinkIdCaptor.capture())
        assertThat(sinkIdCaptor.allValues).hasSize(2)
        assertThat(sinkIdCaptor.allValues[0]).isEqualTo(sink1Id)
        assertThat(sinkIdCaptor.allValues[1]).isEqualTo(sink2Id)
        verify(sinkListener).onAllSinksInitialized()
        verifyNoMoreInteractions(sinkListener)
    }

    // Initialize blocking (with listener), init completed listener

    @Test
    fun shouldInitializeTrackMeBlocking() {
        // given
        given(sink1.consent).willReturn(true)
        given(sink2.consent).willReturn(true)
        whenever(sink1.initialize(any())).then { single(sink1.id, initSuccessConsumer) }
        whenever(sink2.initialize(any())).then { single(sink2.id, initSuccessConsumer) }

        // when
        trackMeSync.initialize(context)

        // then
        verify(sink1).initialize(eq(context))
        verify(sink2).initialize(eq(context))
        verify(sink3, times(2)).initialize(eq(context))
        verify(sinkListener, times(2)).onSinkInitialized(sinkIdCaptor.capture())
        assertThat(sinkIdCaptor.allValues).hasSize(2)
        assertThat(sinkIdCaptor.allValues[0]).isEqualTo(sink1Id)
        assertThat(sinkIdCaptor.allValues[1]).isEqualTo(sink2Id)
        verify(sinkListener).onAllSinksInitialized()
        verifyNoMoreInteractions(sinkListener)
    }

    // Start sinkns in thread, with listener

    @Test
    fun shouldStartTrackMeAsync() {
        // given
        given(sink1.consent).willReturn(true)
        given(sink2.consent).willReturn(true)
        given(sink1.isInitialized()).willReturn(true)
        given(sink2.isInitialized()).willReturn(true)

        whenever(sink1.start()).then { delayedSingle(5, sink1.id, startSuccessConsumer) }
        whenever(sink2.start()).then { delayedSingle(5, sink2.id, startSuccessConsumer) }

        // when
        trackMeAsync.start()

        // then
        verify(sink1).start()
        verify(sink2).start()
        verifyZeroInteractions(sink3)
        verifyZeroInteractions(sinkListener)

        // when
        schedulerRule.testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // then
        verifyZeroInteractions(sink3)
        verifyZeroInteractions(sinkListener)

        // when
        schedulerRule.testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // then
        verify(sink3, times(2)).start()
        verify(sinkListener, times(2)).onSinkStarted(sinkIdCaptor.capture())
        assertThat(sinkIdCaptor.allValues).hasSize(2)
        assertThat(sinkIdCaptor.allValues[0]).isEqualTo(sink1Id)
        assertThat(sinkIdCaptor.allValues[1]).isEqualTo(sink2Id)
        verifyNoMoreInteractions(sinkListener)
    }

    // Start sinkns blocking, with listener

    @Test
    fun shouldStartTrackMeBlocking() {
        // given
        given(sink1.consent).willReturn(true)
        given(sink2.consent).willReturn(true)
        given(sink1.isInitialized()).willReturn(true)
        given(sink2.isInitialized()).willReturn(true)
        whenever(sink1.start()).then { single(sink1.id, startSuccessConsumer) }
        whenever(sink2.start()).then { single(sink2.id, startSuccessConsumer) }

        // when
        trackMeSync.start()

        // then
        verify(sink1).start()
        verify(sink2).start()
        verify(sink3, times(2)).start()
        verify(sinkListener, times(2)).onSinkStarted(sinkIdCaptor.capture())
        assertThat(sinkIdCaptor.allValues).hasSize(2)
        assertThat(sinkIdCaptor.allValues[0]).isEqualTo(sink1Id)
        assertThat(sinkIdCaptor.allValues[1]).isEqualTo(sink2Id)
        verifyNoMoreInteractions(sinkListener)
    }

    // Set consent state to true (from false), threads, listener

    @Test
    fun shouldStartTrackerAsyncWhenConsentSetToTrue() {
        // given
        given(sink1.consent).willReturn(false)
        given(sink1.isInitialized()).willReturn(true)
        whenever(sink1.start()).then { delayedSingle(5, sink1.id, startSuccessConsumer) }

        // when
        trackMeAsync.setConsentTrue(sink1Id)

        // then
        verify(sink1).start()
        verify(sink1).consent = true
        verifyZeroInteractions(sink3)
        verifyZeroInteractions(sinkListener)

        // when
        schedulerRule.testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // then
        verifyZeroInteractions(sink3)
        verifyZeroInteractions(sinkListener)

        // when
        schedulerRule.testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // then
        verify(sink3, times(1)).start()
        verify(sinkListener).onSinkStarted(sinkIdCaptor.capture())
        assertThat(sinkIdCaptor.allValues).hasSize(1)
        assertThat(sinkIdCaptor.allValues[0]).isEqualTo(sink1Id)
        verifyNoMoreInteractions(sinkListener)
    }

    // Set consent state to false (from true), threads, listener

    @Test
    fun shouldFinishTrackerAsyncWhenConsentSetToFalse() {
        // given
        given(sink1.consent).willReturn(true)
        given(sink1.isInitialized()).willReturn(true)
        whenever(sink1.finish()).then { delayedSingle(5, sink1.id, finishSuccessConsumer) }

        // when
        trackMeAsync.setConsentFalse(sink1Id)

        // then
        verify(sink1).finish()
        verify(sink1).consent = false
        verifyZeroInteractions(sink3)
        verifyZeroInteractions(sinkListener)

        // when
        schedulerRule.testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // then
        verifyZeroInteractions(sink3)
        verifyZeroInteractions(sinkListener)

        // when
        schedulerRule.testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // then
        verify(sink3, times(1)).finish()
        verify(sinkListener).onSinkFinished(sinkIdCaptor.capture())
        assertThat(sinkIdCaptor.allValues).hasSize(1)
        assertThat(sinkIdCaptor.allValues[0]).isEqualTo(sink1Id)
        verifyNoMoreInteractions(sinkListener)
    }

    // set consent state true -> true, threads, listener

    @Test
    fun shouldDoNothingAsyncWhenConsentSetToTrueFromTrue() {
        // given
        given(sink1.consent).willReturn(false)
        given(sink1.isInitialized()).willReturn(true)
        whenever(sink1.start()).then { delayedSingle(5, sink1.id, startSuccessConsumer) }

        // when
        trackMeAsync.setConsentFalse(sink1Id)

        // then
        verify(sink1, times(0)).start()
        verify(sink1, times(0)).consent = true
        verifyZeroInteractions(sink3)
        verifyZeroInteractions(sinkListener)

        // when
        schedulerRule.testScheduler.advanceTimeBy(7, TimeUnit.SECONDS)

        // then
        verify(sink1, times(0)).start()
        verify(sink1, times(0)).consent = true
        verify(sink3, times(0)).start()
        verify(sink3, times(0)).consent = true
        verifyZeroInteractions(sinkListener)
    }

    // set consent state false -> false, threads, listener

    @Test
    fun shouldDoNothingAsyncWhenConsentSetToFalseFromFalse() {
        // given
        given(sink1.consent).willReturn(false)
        given(sink1.isInitialized()).willReturn(true)
        whenever(sink1.finish()).then { delayedSingle(5, sink1.id, finishSuccessConsumer) }

        // when
        trackMeAsync.setConsentFalse(sink1Id)

        // then
        verify(sink1, times(0)).finish()
        verify(sink1, times(0)).consent = false
        verifyZeroInteractions(sink3)
        verifyZeroInteractions(sinkListener)

        // when
        schedulerRule.testScheduler.advanceTimeBy(7, TimeUnit.SECONDS)

        // then
        verify(sink1, times(0)).finish()
        verify(sink1, times(0)).consent = false
        verify(sink3, times(0)).finish()
        verify(sink3, times(0)).consent = false
        verifyZeroInteractions(sinkListener)
    }

    // Set consent state to true (from false), listener

    @Test
    fun shouldStartTrackerWhenConsentSetToTrue() {
        // given
        given(sink1.consent).willReturn(false)
        given(sink1.isInitialized()).willReturn(true)
        whenever(sink1.start()).then { single(sink1.id, startSuccessConsumer) }

        // when
        trackMeSync.setConsentTrue(sink1Id)

        // then
        verify(sink1).start()
        verify(sink1).consent = true
        verify(sinkListener).onSinkStarted(sinkIdCaptor.capture())
        assertThat(sinkIdCaptor.allValues).hasSize(1)
        assertThat(sinkIdCaptor.allValues[0]).isEqualTo(sink1Id)
        verifyNoMoreInteractions(sinkListener)
    }

    // Set consent state to false (from true), listener

    @Test
    fun shouldFinishTrackerWhenConsentSetToFalse() {
        // given
        given(sink1.consent).willReturn(true)
        given(sink1.isInitialized()).willReturn(true)
        whenever(sink1.finish()).then { single(sink1.id, finishSuccessConsumer) }

        // when
        trackMeSync.setConsentFalse(sink1Id)

        // then
        verify(sink1).finish()
        verify(sink1).consent = false
        verify(sinkListener).onSinkFinished(sinkIdCaptor.capture())
        assertThat(sinkIdCaptor.allValues).hasSize(1)
        assertThat(sinkIdCaptor.allValues[0]).isEqualTo(sink1Id)
        verifyNoMoreInteractions(sinkListener)
    }

    // set consent state true -> true, listener

    @Test
    fun shouldDoNothingWhenConsentSetToTrueFromTrue() {
        // given
        given(sink1.consent).willReturn(true)
        given(sink1.isInitialized()).willReturn(true)
        whenever(sink1.start()).then { single(sink1.id, startSuccessConsumer) }

        // when
        trackMeSync.setConsentTrue(sink1Id)

        // then
        verify(sink1, times(0)).start()
        verify(sink1, times(0)).consent = true
        verifyZeroInteractions(sinkListener)
    }

    // set consent state false -> false, listener

    @Test
    fun shouldDoNothingWhenConsentSetToFalseFromFalse() {
        // given
        given(sink1.consent).willReturn(false)
        given(sink1.isInitialized()).willReturn(true)
        whenever(sink1.finish()).then { single(sink1.id, finishSuccessConsumer) }

        // when
        trackMeSync.setConsentFalse(sink1Id)

        // then
        verify(sink1, times(0)).finish()
        verify(sink1, times(0)).consent = false
        verifyZeroInteractions(sinkListener)
    }

    // Log event to all, blocking

    @Test
    fun shouldLogEventInBlockingMode() {
        // given
        given(sink1.isInitialized()).willReturn(true)
        given(sink1.consent).willReturn(true)
        given(sink2.isInitialized()).willReturn(true)
        given(sink2.consent).willReturn(true)
        val eventId = "id"
        val event = TargetEvent.Builder(eventId).sink(sink1Id).sink(sink2Id).build()

        // when
        trackMeSync.log(event)

        // then
        verify(sink1).id
        verify(sink1).log(eq(event as Event))
        verify(sink2).id
        verify(sink2).log(eq(event as Event))
    }

    // Log event to all, in threads

    // Log event to one

    @Test
    fun shouldLogEventInBlockingModeToOneSink() {
        // given
        given(sink1.isInitialized()).willReturn(true)
        given(sink1.consent).willReturn(true)
        given(sink2.isInitialized()).willReturn(true)
        given(sink2.consent).willReturn(true)
        val eventId = "id"
        val event = TargetEvent.Builder(eventId).sink(sink1Id).build()

        // when
        trackMeSync.log(event)

        // then
        verify(sink1).id
        verify(sink1).log(eq(event as Event))
        verify(sink2).id
        verify(sink2, times(0)).log(any())
    }

    // Log event to one, in threads

    // Not log if no consent

    @Test
    fun shouldNotLogEventInBlockingModeIfNoConsent() {
        // given
        given(sink1.isInitialized()).willReturn(true)
        given(sink1.consent).willReturn(false)
        val eventId = "id"
        val event = TargetEvent.Builder(eventId).sink(sink1Id).build()

        // when
        trackMeSync.log(event)

        // then
        verify(sink1).id
        verify(sink1, times(0)).log(eq(event as Event))
    }

    // Not log if no consent, in threads

    // Not log if not initialized

    @Test
    fun shouldNotLogEventInBlockingModeIfSinkNotInitialized() {
        // given
        given(sink1.isInitialized()).willReturn(true)
        given(sink1.consent).willReturn(false)
        val eventId = "id"
        val event = TargetEvent.Builder(eventId).sink(sink1Id).build()

        // when
        trackMeSync.log(event)

        // then
        verify(sink1).id
        verify(sink1, times(0)).log(eq(event as Event))
    }

    // Not log if not initialized, in threads

    // Silent crashing in init/start/finish

    @Test
    fun shouldNotCrashWhenInitWithException() {
        // given
        given(sink1.isInitialized()).willReturn(true)
        given(sink1.consent).willReturn(true)
        given(sink1.initialize(any())).willReturn(Single.fromCallable { throw NullPointerException() })

        // when
        trackMeAsync.initialize(context)

        // then
        verifyZeroInteractions(sink3)
        verifyZeroInteractions(sinkListener)

        // when
        schedulerRule.testScheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        // then
        verifyZeroInteractions(sink3)
        verify(sinkListener).onError(any())
    }

    @Test
    fun shouldNotCrashWhenStartWithException() {
        // given
        given(sink1.isInitialized()).willReturn(true)
        given(sink1.consent).willReturn(true)
        given(sink1.start()).willReturn(Single.fromCallable { throw NullPointerException() })

        // when
        trackMeAsync.start()

        // then
        verifyZeroInteractions(sink3)
        verify(sinkListener).onError(any())
    }

    @Test
    fun shouldNotCrashWhenFinishWithException() {
        // given
        given(sink1.isInitialized()).willReturn(true)
        given(sink1.consent).willReturn(true)
        given(sink1.finish()).willReturn(Single.fromCallable { throw NullPointerException() })

        // when
        trackMeAsync.finish()

        // then
        verifyZeroInteractions(sink3)
        verify(sinkListener).onError(any())
    }

    // Silent crashing in logging

    @Test
    fun shouldNotCrashWhenLogWithException() {
        // given
        given(sink1.isInitialized()).willReturn(true)
        given(sink1.consent).willReturn(true)
        given(sink1.log(any())).willReturn(Single.fromCallable { throw NullPointerException() })
        val eventId = "id"
        val event = TargetEvent.Builder(eventId).sink(sink1Id).build()

        // when
        trackMeSync.log(event)

        // then
        verifyZeroInteractions(sink3)
        verify(sinkListener).onError(any())
    }

    // Overriding all consents to true

    @Test
    fun shouldLogEventEvenWhenConsentNotGiven() {
        // given
        given(sink1.isInitialized()).willReturn(true)
        given(sink1.consent).willReturn(false)
        given(sink2.isInitialized()).willReturn(true)
        given(sink2.consent).willReturn(true)
        val eventId = "id"
        val event = TargetEvent.Builder(eventId).sink(sink1Id).sink(sink2Id).build()
        val trackMe = TrackMe.Builder()
            .withBlocking(true)
            .withConsentOverride(overrideConsent = true, overrideValue = true)
            .withSink(sink1)
            .withSink(sink2)
            .build()

        // when
        trackMe.log(event)

        // then
        verify(sink1).log(eq(event as Event))
        verify(sink2).log(eq(event as Event))
    }

    // Overriding all consents to false

    @Test
    fun shouldNotLogEventEvenWhenConsentIsGiven() {
        // given
        given(sink1.isInitialized()).willReturn(true)
        given(sink1.consent).willReturn(false)
        given(sink2.isInitialized()).willReturn(true)
        given(sink2.consent).willReturn(true)
        val eventId = "id"
        val event = TargetEvent.Builder(eventId).sink(sink1Id).sink(sink2Id).build()
        val trackMe = TrackMe.Builder()
            .withBlocking(true)
            .withConsentOverride(overrideConsent = true, overrideValue = false)
            .withSink(sink1)
            .withSink(sink2)
            .build()

        // when
        trackMe.log(event)

        // then
        verify(sink1, times(0)).log(eq(event as Event))
        verify(sink2, times(0)).log(eq(event as Event))
    }

    // Finish all sinks, listener

    @Test
    fun shouldFinishAllSinks() {
        // given
        whenever(sink1.finish()).then { single(sink1.id, finishSuccessConsumer) }
        whenever(sink2.finish()).then { single(sink2.id, finishSuccessConsumer) }

        // when
        trackMeSync.finish()

        // then
        verify(sink1).finish()
        verify(sink2).finish()
        verify(sink3, times(2)).finish()
        verify(sinkListener, times(2)).onSinkFinished(sinkIdCaptor.capture())
        assertThat(sinkIdCaptor.allValues).hasSize(2)
        assertThat(sinkIdCaptor.allValues[0]).isEqualTo(sink1Id)
        assertThat(sinkIdCaptor.allValues[1]).isEqualTo(sink2Id)
        verifyNoMoreInteractions(sinkListener)
    }

    // Finish all sinks, thread, listener

    @Test
    fun shouldFinishAsyncAllSinks() {
        // given
        whenever(sink1.finish()).then { delayedSingle(5, sink1.id, finishSuccessConsumer) }
        whenever(sink2.finish()).then { delayedSingle(5, sink2.id, finishSuccessConsumer) }

        // when
        trackMeAsync.finish()

        // then
        verify(sink1).finish()
        verify(sink2).finish()
        verifyZeroInteractions(sink3)
        verifyZeroInteractions(sinkListener)

        // when
        schedulerRule.testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // then
        verifyZeroInteractions(sink3)
        verifyZeroInteractions(sinkListener)

        // when
        schedulerRule.testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        // then
        verify(sink3, times(2)).finish()
        verify(sinkListener, times(2)).onSinkFinished(sinkIdCaptor.capture())
        assertThat(sinkIdCaptor.allValues).hasSize(2)
        assertThat(sinkIdCaptor.allValues[0]).isEqualTo(sink1Id)
        assertThat(sinkIdCaptor.allValues[1]).isEqualTo(sink2Id)
        verifyNoMoreInteractions(sinkListener)
    }

    // Helpers

    private fun delayedSingle(seconds: Long, hashable: Hashable, onSuccess: Consumer<Pair<Hashable, Boolean>>) =
        Single.just(Pair(hashable, true))
            .delay(seconds, TimeUnit.SECONDS, schedulerRule.testScheduler)
            .doOnSuccess(onSuccess)

    private fun single(hashable: Hashable, onSuccess: Consumer<Pair<Hashable, Boolean>>) =
        Single.just(Pair(hashable, true)).doOnSuccess(onSuccess)
}
