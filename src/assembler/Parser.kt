package assembler

import com.fox.general.LongExtension
import com.fox.io.log.ConsoleLogger
import model.ReversibleIterator
import model.size
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

    val byteBuffer = ByteBuffer.allocate(java.lang.Long.BYTES)

    linesOfTokens.forEach {
        println(it)
        val parseBinary = parseTokenLine(it)
        val endianFix = fixEndian(parseBinary)
        ConsoleLogger.debug("endianFix = $endianFix")

        val tryParse = LongExtension.tryParse(endianFix, 2)

        if (tryParse.first) {
            ConsoleLogger.debug("Long that I'm 'putting' = ${tryParse.second}")
            byteBuffer.putLong(tryParse.second)
        }
    }

    val bytes = byteBuffer.array()
    bytes.writeToFile(outputFile, false)

    println(byteBuffer)
    return outputFile.toPath()
}

/**
 * takes a binary string. otherwise error
 */
private fun fixEndian(parseBinary : String) : String {
    val reversedArray = parseBinary.splitEvery(8)
            .reversedArray()
    ConsoleLogger.debug("Reversed binary as Array = ${reversedArray.joinToString()}")
    ConsoleLogger.debug("The above as hex " +
            "${reversedArray.map { Integer.toHexString(Integer.parseInt(it, 2)) }}")
    val endianFix = reversedArray
            .reduce({ acc, curr -> acc.plus(curr) })
    return endianFix
}

private fun parseTokenLine(tokenStream : TokenStream) : String {

    val tokenIterator = tokenStream.iterator()

    val token = tokenIterator.next()
    var ret = when (token) {
        is LoadCommand -> loadParseLogic(token, tokenIterator)
        is AddOperationToken -> "" // TODO: this AddOperationToken needs doing
        is OrOperationToken -> "" // TODO: this OrOperationToken needs doing
        is StoreOperationToken -> "" // TODO: this StoreOperationToken needs doing
        is MoveCommand -> "" // TODO: this MoveCommand needs doing
        is BranchCommand -> "" // TODO: this BranchCommand needs doing
        is SubtractOperationToken -> "" // TODO: this SubtractOperationToken needs doing
        else -> "" // shouldn't be able to make it here. Should I throw an error?
    }

    return ret
}

// returns the binary string of the operation encoding
fun loadParseLogic(loadToken: LoadCommand, iterator: ReversibleIterator<Token>) : String {
    val builder = StringBuilder(32)

    var PUWTriple = Triple('1', '1', '0') // Bits P, U, and W ALL equal '0'

    val iteratorSize = iterator.size()
    ConsoleLogger.debug("Iterator.size = $iteratorSize")
    when (iteratorSize) {
        3 -> {
            // treat like a 4-token instruction, with '0' for an immediate value
            val registerDest = iterator.next() as RegisterToken
            val registerSource = iterator.next() as RegisterToken
            val immOpCode = "010" + PUWTriple.first + PUWTriple.second + '0' + PUWTriple.third + '1'
            builder.append(Integer.toBinaryString(loadToken.conditionInt))
            builder.append(immOpCode)
            builder.append(registerSource.nibble)
            builder.append(registerDest.nibble)
            // pad the last imm12 bits with 0s
            builder.append("000000000000")
        }
    }
    val toString = builder.toString()

    val binAsInt = java.lang.Long.parseLong(toString, 2)
    val hexString = java.lang.Long.toHexString(binAsInt)
    ConsoleLogger.debug("Built binary $toString. Hex: $hexString")

    return toString
}