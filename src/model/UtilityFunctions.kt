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

fun ByteArray.writeToFile(file : File) : Unit {
    val fileOutputStream = FileOutputStream(file)
    fileOutputStream.write(this)
    fileOutputStream.flush()
    fileOutputStream.close()
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
fun String.splitOn(n : Int) : Array<String> {
//    this.spl
    return emptyArray()
}