package eu.codingtemple.trackme

import android.content.Context

interface Sink {
    val id: Hashable

    var consent: Boolean

    fun log(event: Event): String

    fun start(context: Context)

    fun finish()
}
