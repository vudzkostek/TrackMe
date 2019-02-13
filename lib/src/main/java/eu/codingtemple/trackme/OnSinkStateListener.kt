package eu.codingtemple.trackme

interface OnSinkStateListener {
    fun onSinkCreated(sinkId: Hashable)

    fun onSinkStarted(sinkId: Hashable)

    fun onSinkFinished(sinkId: Hashable)
}
