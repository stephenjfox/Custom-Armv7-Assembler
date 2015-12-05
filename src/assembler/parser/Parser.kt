package assembler.parser

import assembler.*
import com.fox.general.LongExtension
import com.fox.io.log.ConsoleLogger
import model.GlobalConfig
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
        println(stream)
        val parseBinary = parseTokenLine(stream)
        val endianFix = fixEndian(parseBinary)

        if (GlobalConfig.getBoolean("verbose")) {
            ConsoleLogger.debug("endianFix = $endianFix")
        }

        val tryParse = LongExtension.tryParse(endianFix, 2)

        if (tryParse.first) {
            ConsoleLogger.debug("Long that I'm 'putting' = ${tryParse.second}")
            byteBuffer.putInt(tryParse.second.toInt())

            val bytes = byteBuffer.array()
            bytes.writeToFile(outputFile, index == linesOfTokens.lastIndex)

            println(byteBuffer)
        }
    }


    return outputFile.toPath()
}

/**
 * takes a binary string. otherwise error
 */
private fun fixEndian(parseBinary : String) : String {
    val reversedArray = parseBinary.splitEvery(8)
            .reversedArray()
    if (GlobalConfig.getBoolean("verbose")) {
        ConsoleLogger.debug("Reversed binary as Array = ${reversedArray.joinToString()}")
        ConsoleLogger.debug("The above as hex " +
                "${reversedArray.map { Integer.toHexString(Integer.parseInt(it, 2)) }}")
    }
    val endianFix = reversedArray
            .reduce({ acc, curr -> acc.plus(curr) })
    return endianFix
}

private fun parseTokenLine(tokenStream : TokenStream) : String {

    val tokenIterator = tokenStream.iterator()

    val token = tokenIterator.next()
    var ret = when (token) {
        is LoadCommand,
        is StoreOperationToken -> loadStoreParseLogic(token as CommandToken, tokenIterator)
        is AddOperationToken -> "" // TODO: this AddOperationToken needs doing
        is SubtractOperationToken -> "" // TODO: this SubtractOperationToken needs doing
        is OrOperationToken -> "" // TODO: this OrOperationToken needs doing
        is MoveCommand -> "" // TODO: this MoveCommand needs doing
        is BranchCommand -> "" // TODO: this BranchCommand needs doing
        else -> "" // shouldn't be able to make it here. Should I throw an error?
    }

    return ret
}
