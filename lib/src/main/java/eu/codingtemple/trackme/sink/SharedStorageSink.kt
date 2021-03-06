package eu.codingtemple.trackme.sink

abstract class SharedStorageSink<T : ConsentStorage> : Sink {
    internal abstract val storage: T

    override var consent: Boolean
        get() = storage.getConsent(id)
        set(consent) = storage.setConsent(id, consent)
}

