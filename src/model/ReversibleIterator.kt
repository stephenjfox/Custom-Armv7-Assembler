package model

interface ReversibleIterator<T> : Iterator<T> {
    fun canReverseNext() : Boolean
    fun reverseNext() : T
    fun skipToEnd()
}