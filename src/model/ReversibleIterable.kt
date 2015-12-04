package model

/**
 * Created by stephen on 11/29/15.
 */
interface ReversibleIterable<T> : Iterable<T> {
    override fun iterator() : ReversibleIterator<T>
}