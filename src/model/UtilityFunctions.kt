package model

/**
 * Created by stephen on 12/1/15.
 */
fun CharSequence.asString() : String {
    return this.repeat(1)
}

fun String.splitOnSpace(vararg ignore: Char) : List<String> {
    return this.split(' ').map { it.trimEnd { it in ignore } }
}