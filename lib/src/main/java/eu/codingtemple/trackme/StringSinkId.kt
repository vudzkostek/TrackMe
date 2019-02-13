package eu.codingtemple.trackme

class StringSinkId(private val id: String) : Hashable() {

    override fun eq(other: Any?): Boolean {
        return id == other
    }

    override fun hash(): Int {
        return id.hashCode()
    }

    override fun string(): String {
        return id
    }
}
