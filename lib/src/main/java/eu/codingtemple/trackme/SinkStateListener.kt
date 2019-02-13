package eu.codingtemple.trackme

interface SinkStateListener {
    fun onSinkCreated(sinkId: Hashable)

    fun onSinkStarted(sinkId: Hashable)

    fun onSinkFinished(sinkId: Hashable)

    fun onAllSinksStarted()

    fun onError(throwable: Throwable)
}
