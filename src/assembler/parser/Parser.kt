package assembler.parser

import assembler.*
import com.fox.general.LongExtension
import model.Logger
import model.splitEvery
import model.writeToFile
import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Path

/**
 *
 * Created by stephen on 12/3/15.
 */
fun parseTokensToFile(tokenStream : TokenStream, outputFile : File) : Path {
    val linesOfTokens = tokenStream.spliceLines()

    // ARMv7-R 'word's are 32-bit, so we use Integer for this :P
    val byteBuffer = ByteBuffer.allocate(Integer.BYTES * linesOfTokens.size)

    linesOfTokens.forEachIndexed { index, stream ->
        Logger.d("Working on stream: $stream")

        val parseBinary = parseTokenLine(stream)
        val endianFix = fixEndian(parseBinary)

        Logger.d("endianFix = $endianFix")

        val tryParse = LongExtension.tryParse(endianFix, 2)

        if (tryParse.first) {
            Logger.d("Long that I'm 'putting' = ${tryParse.second}")
            byteBuffer.putInt(tryParse.second.toInt())

            val bytes = byteBuffer.array()
            bytes.writeToFile(outputFile, index == linesOfTokens.lastIndex)

            println(byteBuffer)
        }
    }

    return outputFile.toPath()
}

/**
 * Parses a [TokenStream], by determining the operation by the stream's lead
 * [Token] and fulfilling a selected functions body with the remainder of the stream
 */
internal fun parseTokenLine(tokenStream : TokenStream) : String {

    val tokenIterator = tokenStream.iterator()

    val token = tokenIterator.next()
    var ret = when (token) {
        is LoadOperationToken, is StoreOperationToken
            -> loadStoreOperationParse(token as CommandToken, tokenIterator)
        is SubtractOperationToken, is AddOperationToken
            -> addSubOperationParse(token as DataOperationCommandToken, tokenIterator)
        is OrOperationToken
            -> orOperationParse(token, tokenIterator)
        is MoveCommand -> moveCommandParse(token, tokenIterator)
        is BranchCommand, is BranchWithLinkCommand
            -> branchOperationParse(token as CommandToken, tokenIterator.next())
        is PopManyToken, is PushManyToken
            -> pushPopOperationParse(token as CommandToken, tokenIterator)
        else -> throw IllegalStateException("Token argument was invalid $token")
    }

    return ret
}

/**
 * takes a binary string. otherwise error
 */
private fun fixEndian(parseBinary : String) : String {
    val reversedArray = parseBinary.splitEvery(8)
            .reversedArray()

    Logger.v("Reversed binary as Array = ${reversedArray.joinToString()}")
    Logger.v("The above as hex " +
            "${reversedArray.map { Integer.toHexString(Integer.parseInt(it, 2)) }}")

    val endianFix = reversedArray
            .reduce({ acc, curr -> acc.plus(curr) })
    return endianFix
}
