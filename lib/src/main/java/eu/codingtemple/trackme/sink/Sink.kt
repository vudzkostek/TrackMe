package eu.codingtemple.trackme.sink

import android.content.Context
import eu.codingtemple.trackme.event.Event
import io.reactivex.Single

interface Sink {
    val id: Hashable

    var consent: Boolean

    fun initialize(context: Context): Single<Pair<Hashable, Boolean>>

    fun isInitialized(): Boolean

    fun log(event: Event): Single<Pair<Hashable, Boolean>>

    fun start(): Single<Pair<Hashable, Boolean>>

    fun finish(): Single<Pair<Hashable, Boolean>>
}
