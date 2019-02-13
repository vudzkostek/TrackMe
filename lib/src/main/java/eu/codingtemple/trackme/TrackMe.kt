package eu.codingtemple.trackme

import android.content.Context
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.*

class TrackMe private constructor(builder: Builder) {

    private var disposable: Disposable? = null
    private val scheduler = Schedulers.io()

    private val errorConsumer = Consumer<Throwable> {
        sinkListener?.onError(it)

        if (!silentCrashing) {
            throw it
        }
    }
    private val completeAction = Action {
        sinkListener?.onAllSinksStarted()
    }

    private val sinks = mutableMapOf<Hashable, Sink>()
    private val overrideConsent: Boolean
    private val overrideValue: Boolean
    private val silentCrashing: Boolean
    private val sinkListener: SinkStateListener?
    private var blocking: Boolean


    init {
        for (sink in builder.sinks) {
            sinks[sink.id] = sink
        }
        this.overrideConsent = builder.overrideConsent
        this.overrideValue = builder.overrideValue
        this.silentCrashing = builder.silentCrashing
        this.sinkListener = builder.sinkListener
        this.blocking = builder.blocking
    }

    fun start(context: Context) {
        val observable = Observable.fromArray(sinks.values).flatMapIterable { it }

        if (blocking) {
            observable.blockingSubscribe(
                Consumer {
                    if (it.consent) {
                        it.start(context)
                        sinkListener?.onSinkStarted(it.id)
                    }
                }, errorConsumer, completeAction
            )
        } else {
            disposable = observable.observeOn(scheduler)
                .subscribe(
                    Consumer {
                        if (it.consent) {
                            it.start(context)
                            sinkListener?.onSinkStarted(it.id)
                        }
                    }, errorConsumer, completeAction
                )
        }
    }

    fun setConsentTrue(sinkIds: List<Hashable>, context: Context) {
        val observable = Observable.fromArray(sinks.values).flatMapIterable { it }

        if (blocking) {
            observable.blockingSubscribe(
                Consumer {
                    if(sinkIds.contains(it.id)) {
                        val consentOld = it.consent
                        it.consent = true

                        if (!consentOld) {
                            it.start(context)
                            sinkListener?.onSinkStarted(it.id)
                        }
                    }
                }, errorConsumer, completeAction
            )
        } else {
            disposable = observable.observeOn(scheduler)
                .subscribe(
                    Consumer {
                        if(sinkIds.contains(it.id)) {
                            val consentOld = it.consent
                            it.consent = true

                            if (!consentOld) {
                                it.start(context)
                                sinkListener?.onSinkStarted(it.id)
                            }
                        }
                    }, errorConsumer, completeAction
                )
        }
    }

    fun setConsentTrue(sinkId: Hashable, context: Context) {
        setConsentTrue(listOf(sinkId), context)
    }

    fun setConsentFalse(sinkId: Hashable) {
        val sink = sinks[sinkId] ?: throw RuntimeException("Sink $sinkId was not added to TrackMe")

        val consentOld = sink.consent
        sink.consent = false

        if (consentOld) {
            sink.finish()
        }
    }

    fun finishAll() {
        sinks.values.forEach { it.finish() }
    }

    inner class Builder {
        internal val sinks = ArrayList<Sink>()
        internal var overrideConsent: Boolean = false
        internal var overrideValue: Boolean = false
        internal var silentCrashing = true
        internal var sinkListener: SinkStateListener? = null
        internal var blocking: Boolean = false

        fun withSink(sink: Sink): Builder {
            sinks.add(sink)
            return this
        }

        fun withConsentOverride(overrideConsent: Boolean, overrideValue: Boolean): Builder {
            this.overrideConsent = overrideConsent
            this.overrideValue = overrideValue
            return this
        }

        fun withSilentCrashing(silentCrashing: Boolean): Builder {
            this.silentCrashing = silentCrashing
            return this
        }

        fun withBlocking(blocking: Boolean): Builder {
            this.blocking = blocking
            return this
        }

        fun withSinkListener(sinkListener: SinkStateListener): Builder {
            this.sinkListener = sinkListener
            return this
        }

        fun build(): TrackMe {
            return TrackMe(this)
        }
    }
}
