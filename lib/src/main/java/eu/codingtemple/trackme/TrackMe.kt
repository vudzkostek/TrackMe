package eu.codingtemple.trackme

import android.content.Context
import eu.codingtemple.trackme.event.TargetEvent
import eu.codingtemple.trackme.sink.Hashable
import eu.codingtemple.trackme.sink.Sink
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.merge
import io.reactivex.schedulers.Schedulers
import java.util.*

class TrackMe private constructor(builder: Builder) {

    private val scheduler: Scheduler
    private var disposable: Disposable? = null

    private val errorConsumer = Consumer<Throwable> {
        sinkListener?.onError(it)
    }

    val sinks = mutableListOf<Sink>()
    val overrideConsent: Boolean
    val overrideValue: Boolean
    val sinkListener: SinkStateListener?
    var blocking: Boolean

    init {
        this.sinks.addAll(builder.sinks)
        this.overrideConsent = builder.overrideConsent
        this.overrideValue = builder.overrideValue
        this.sinkListener = builder.sinkListener
        this.blocking = builder.blocking

        scheduler = if (blocking) {
            Schedulers.trampoline()
        } else {
            Schedulers.io()
        }
    }

    fun log(targetEvent: TargetEvent) {
        // TODO thread
        sinks.forEach {
            if (targetEvent.getTargetSinks().contains(it.id) && it.isInitialized() && consent(it)) {
                it.log(targetEvent)
            }
        }
    }

    private fun consent(sink: Sink) = (!overrideConsent && sink.consent) || (overrideConsent && overrideValue)

    fun initialize(context: Context) {
        val observable = sinks
            .filter { consent(it) }
            .map { it.initialize(context).toObservable() }
            .merge()
            .subscribeOn(scheduler)
            .observeOn(scheduler)

        if (blocking) {
            observable.blockingForEach { pair -> sinkListener?.onSinkInitialized(pair.first) }
            sinkListener?.onAllSinksInitialized()
        } else {
            disposable = observable.subscribe({ pair ->
                sinkListener?.onSinkInitialized(pair.first)
            }, {
                sinkListener?.onSinkInitError(it)
            }, {
                sinkListener?.onAllSinksInitialized()
            })
        }
    }

    fun start() {
        val observable = sinks
            .filter { consent(it) && it.isInitialized() }
            .map { it.start().toObservable() }
            .merge()
            .subscribeOn(scheduler)
            .observeOn(scheduler)

        if (blocking) {
            observable.blockingForEach { pair -> sinkListener?.onSinkStarted(pair.first) }
        } else {
            disposable = observable.subscribe({ pair ->
                sinkListener?.onSinkStarted(pair.first)
            }, {
                sinkListener?.onError(it)
            })
        }
    }

    fun setConsentTrue(sinkIds: List<Hashable>) {
        val observable = sinks
            .filter { sinkIds.contains(it.id) && !it.consent }
            .map {
                it.consent = true
                it.start().toObservable()
            }
            .merge()
            .subscribeOn(scheduler)
            .observeOn(scheduler)

        if (blocking) {
            observable.blockingForEach { pair -> sinkListener?.onSinkStarted(pair.first) }
        } else {
            disposable = observable.subscribe({ pair ->
                sinkListener?.onSinkStarted(pair.first)
            }, {
                sinkListener?.onError(it)
            })
        }
    }

    fun setConsentTrue(sinkId: Hashable) {
        setConsentTrue(listOf(sinkId))
    }

    fun setConsentFalse(sinkIds: List<Hashable>) {
        val observable = sinks
            .filter { sinkIds.contains(it.id) && it.consent }
            .map {
                it.consent = false
                it.finish().toObservable()
            }
            .merge()
            .subscribeOn(scheduler)
            .observeOn(scheduler)

        if (blocking) {
            observable.blockingForEach { pair -> sinkListener?.onSinkFinished(pair.first) }
        } else {
            disposable = observable.subscribe({ pair ->
                sinkListener?.onSinkFinished(pair.first)
            }, {
                sinkListener?.onError(it)
            })
        }
    }

    fun setConsentFalse(sinkId: Hashable) {
        setConsentFalse(listOf(sinkId))
    }

    fun finish() {
        val observable = sinks
            .map { it.finish().toObservable() }
            .merge()
            .subscribeOn(scheduler)
            .observeOn(scheduler)

        if (blocking) {
            observable.blockingForEach { pair -> sinkListener?.onSinkFinished(pair.first) }
        } else {
            disposable = observable.subscribe({ pair ->
                sinkListener?.onSinkFinished(pair.first)
            }, {
                sinkListener?.onError(it)
            })
        }
    }

    class Builder {
        internal val sinks = ArrayList<Sink>()
        internal var overrideConsent = false
        internal var overrideValue = false
        internal var sinkListener: SinkStateListener? = null
        internal var blocking = false

        fun withSink(sink: Sink): Builder {
            sinks.add(sink)
            return this
        }

        fun withConsentOverride(overrideConsent: Boolean, overrideValue: Boolean): Builder {
            this.overrideConsent = overrideConsent
            this.overrideValue = overrideValue
            return this
        }

        fun withBlocking(blocking: Boolean): Builder {
            this.blocking = blocking
            return this
        }

        fun withSinkListener(sinkListener: SinkStateListener?): Builder {
            this.sinkListener = sinkListener
            return this
        }

        fun build(): TrackMe {
            return TrackMe(this)
        }
    }
}
