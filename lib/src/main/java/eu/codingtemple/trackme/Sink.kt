package eu.codingtemple.trackme

import android.content.Context

interface Sink {
    val id: Hashable

    var consent: Boolean

    fun initialize(context: Context)

    fun log(event: Event)

    fun start()

    fun finish()
}
