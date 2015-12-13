package assembler

import model.ReversibleIterable
import model.ReversibleIterator
import java.util.*

/**
 * Created by stephen on 11/28/15.
 */
class TokenStream(val streamSource: List<Token>) : ReversibleIterable<Token> {

    val size: Int
        get () {
            return streamSource.size
        }

    constructor(streamSource: Array<Token>) : this(streamSource.asList())

    override fun iterator() : ReversibleIterator<Token> {
        return TokenStreamIterator()
    }

    override fun toString() : String {
        return streamSource.toString()
    }

    private inner class TokenStreamIterator : ReversibleIterator<Token> {

        private var cursor : Int = 0

        override fun reverseNext() : Token = streamSource[--cursor]

        override fun next() : Token = streamSource[cursor++]

        override fun hasNext() : Boolean = cursor < streamSource.size

        override fun canReverseNext() : Boolean = cursor > 0

        override fun skipToEnd() {
            while(this.hasNext()) this.next()
        }
    }

    companion object {

        private var yieldList : Vector<Token> = Vector()

        val willYield : Boolean
            get () = yieldList.size > 0

        val emptyStream : TokenStream
            get () = TokenStream(emptyList())

        fun yield(token : Token) {
            yieldList.add(token)
        }

        fun of(vararg tokens : Token) : TokenStream {
            val ret = Vector<Token>()
            tokens.forEach { ret.add(it) }
            return TokenStream(ret)
        }

        fun yieldSequential(tokens: Iterable<Token>) {
            for (token in tokens) {
                yield(token)
            }
        }

        fun yieldSequential(vararg tokens : Token) {
            for (token in tokens) {
                yield(token)
            }
        }

        fun getRawYieldList() : List<Token> {
            val ret = yieldList.toList() // give me the same values, in a different heap reference

            yieldList.clear() // empty the once shared reference

            return ret
        }

        fun getTokenStream() : TokenStream {

            val ret = TokenStream(yieldList)

            yieldList = Vector() // give it a new reference on the heap

            return ret
        }

    }

}

