package model

/**
 * Created by stephen on 11/28/15.
 */
class ReversibleIntIterator(private val list : List<Int>) : ReversibleIterator<Int> {
    private var cursor : Int = 0

    override fun next() : Int = list[cursor++]

    override fun hasNext() : Boolean = cursor < list.size

    override fun reverseNext() : Int = list[--cursor]

    override fun canReverseNext() : Boolean = cursor > 0

    override fun skipToEnd() {
        while (hasNext()) next()
    }
}