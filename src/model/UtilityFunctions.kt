package model

import java.io.File
import java.io.FileOutputStream

/**
 * Created by stephen on 12/1/15.
 */
fun CharSequence.asString() : String {
    return this.repeat(1)
}

fun String.splitOnSpace(vararg ignore: Char) : List<String> {
    return this.split(' ').map { it.trimEnd { it in ignore } }
}

fun ByteArray.writeToFile(file : File, flushAndClose : Boolean = true) : Unit {
    val fileOutputStream = FileOutputStream(file)
    fileOutputStream.write(this)
    if (flushAndClose) {
        fileOutputStream.flush()
        fileOutputStream.close()
    }
}

fun <T> ReversibleIterator<T>.size() : Int {
    var resetCount = 0
    // get to the head
    while (this.canReverseNext()) {
        this.reverseNext()
        resetCount += 1
    }

    // count em
    var count = 0
    while (this.hasNext()) {
        this.next()
        count += 1
    }

    // rollback
    while (this.canReverseNext()) {
        this.reverseNext()
    }

    // reset where we were
    for (i in 1..resetCount) {
        this.next()
    }

    return count
}

/**
 * Return an array of String split every n-characters
 */
fun String.splitEvery(n : Int) : Array<String> {
    val properLength = this.length / n + (if (this.length % n == 0) 0 else 1)

    val subStrings = Array(properLength, { ind ->
        val startInd = ind * n
        if (ind == properLength) {
            this.substring(startIndex = startInd, endIndex = this.length)
        } else this.substring(startIndex = startInd, endIndex = startInd + n)
    })
    return subStrings
}

fun String.Companion.empty() : String {
    return ""
}

infix fun Int.rotateRight(distance : Int) : Int {
    return Integer.rotateRight(this, distance)
}

infix fun Int.rotateLeft(distance : Int) : Int {
    return Integer.rotateLeft(this, distance)
}

fun Int.toBinaryString() : String {
    return Integer.toBinaryString(this)
}

fun not (int : Int) : Int {
    if (int == Int.MIN_VALUE) return Int.MAX_VALUE
    return Int.MAX_VALUE xor int
}