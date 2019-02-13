package eu.codingtemple.trackme.sink

class StringSinkId(private val id: String) : Hashable() {

    override fun eq(other: Any?): Boolean {
        return id == if (other is StringSinkId) other.id else null
    }

    override fun hash(): Int {
        return id.hashCode()
    }

    override fun string(): String {
        return id
    }
}
