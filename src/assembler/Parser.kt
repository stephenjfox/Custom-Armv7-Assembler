package assembler

import model.ReversibleIterable
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.file.Path

/**
 * Created by stephen on 12/3/15.
 */
fun parseTokensToFile(tokenStream : TokenStream, outputFile : File) : Path {
    val linesOfTokens = tokenStream.spliceLines()

    val byteBuffer = ByteBuffer.allocate(Integer.BYTES * linesOfTokens.size)

    linesOfTokens.forEach {
        val parseResult = parseTokens(it)
        byteBuffer.putInt(parseResult)
    }

    val bytes = byteBuffer.array()
    bytes.writeToFile(outputFile)

    return outputFile.toPath()
}

private fun parseTokens(tokenIterable : ReversibleIterable<Token>) : Int {

    return -1
}

fun ByteArray.writeToFile(file : File) : Unit {
    val fileOutputStream = FileOutputStream(file)
    fileOutputStream.write(this)
    fileOutputStream.flush()
    fileOutputStream.close()
}