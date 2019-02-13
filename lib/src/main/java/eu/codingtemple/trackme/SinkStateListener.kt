package eu.codingtemple.trackme

import eu.codingtemple.trackme.sink.Hashable

interface SinkStateListener {
    fun onSinkInitialized(sinkId: Hashable)

    fun onSinkInitError(throwable: Throwable)

    fun onSinkStarted(sinkId: Hashable)

    fun onSinkFinished(sinkId: Hashable)

    fun onAllSinksInitialized()

    fun onError(throwable: Throwable)
}
