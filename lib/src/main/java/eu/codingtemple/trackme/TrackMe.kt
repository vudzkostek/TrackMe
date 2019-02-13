package eu.codingtemple.trackme

import android.content.Context
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class TrackMe private constructor(builder: Builder) {

    private var disposable: Disposable? = null
    private val scheduler = Schedulers.io()

    private val sinks = mutableMapOf<Hashable, Sink>()
    private val overrideConsent: Boolean
    private val overrideValue: Boolean
    private val silentCrashing: Boolean
    private val sinkListener: SinkStateListener?

    init {
        for (sink in builder.sinks) {
            sinks[sink.id] = sink
        }
        this.overrideConsent = builder.overrideConsent
        this.overrideValue = builder.overrideValue
        this.silentCrashing = builder.silentCrashing
        this.sinkListener = builder.sinkListener
    }


    fun start(context: Context) {
        disposable = Observable.fromArray(sinks.values)
            .flatMapIterable { it }
            .observeOn(scheduler)
            .subscribe({
                if (it.consent) {
                    it.start(context)
                }
            }, {
                sinkListener?.onError(it)

                if (!silentCrashing) {
                    throw it
                }
            }, {
                sinkListener?.onAllSinksStarted()
            })

        for (sink in sinks.values) {
            if (sink.consent) {
                sink.start(context)
            }
        }
    }

    inner class Builder {
        internal val sinks = ArrayList<Sink>()
        internal var overrideConsent: Boolean = false
        internal var overrideValue: Boolean = false
        internal var silentCrashing = true
        internal var sinkListener: SinkStateListener? = null

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

        fun withSinkListener(sinkListener: SinkStateListener): Builder {
            this.sinkListener = sinkListener
            return this
        }

        fun build(): TrackMe {
            return TrackMe(this)
        }
    }
}
