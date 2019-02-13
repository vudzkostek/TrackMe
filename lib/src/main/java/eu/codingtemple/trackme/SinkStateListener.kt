package eu.codingtemple.trackme

interface SinkStateListener {
    fun onSinkInitialized(sinkId: Hashable)

    fun onSinkStarted(sinkId: Hashable)

    fun onSinkFinished(sinkId: Hashable)

    fun onAllSinksInitialized()

    fun onError(throwable: Throwable)
}
