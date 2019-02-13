package eu.codingtemple.trackme.sink

abstract class Hashable {
    abstract fun eq(o: Any?): Boolean

    abstract fun hash(): Int

    abstract fun string(): String

    override fun equals(other: Any?): Boolean {
        return eq(other)
    }

    override fun hashCode(): Int {
        return hash()
    }

    override fun toString(): String {
        return string()
    }
}
