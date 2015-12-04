package assembler

import com.fox.general.LongExtension
import com.fox.io.log.ConsoleLogger
import model.ReversibleIterator
import model.size
import model.writeToFile
import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Path

/**
 * Created by stephen on 12/3/15.
 */
fun parseTokensToFile(tokenStream : TokenStream, outputFile : File) : Path {
    val linesOfTokens = tokenStream.spliceLines()

    val byteBuffer = ByteBuffer.allocate(Integer.BYTES * linesOfTokens.size)

    linesOfTokens.forEach {
        println(it)
        val parseBinary = parseTokenLine(it)
        val tryParse = LongExtension.tryParse(parseBinary, 2)
        //        val parseResult = Long.parseInt(parseBinary, 2)

        if (tryParse.first) {
            ConsoleLogger.debug("Long that I'm 'putting' = ${tryParse.second}")
            byteBuffer.putLong(tryParse.second)
        }
    }

    val bytes = byteBuffer.array()
    println(byteBuffer)
    bytes.writeToFile(outputFile)

    return outputFile.toPath()
}

private fun parseTokenLine(tokenStream : TokenStream) : String {

    val tokenIterator = tokenStream.iterator()

    val token = tokenIterator.next()
    var ret = ""
    when (token) {
        is LoadCommand -> ret = loadParseLogic(token, tokenIterator)
    }

    return ret
}

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
    ConsoleLogger.debug("Built binary $toString. Hex: ${java.lang.Long.toHexString(binAsInt)}")

    return toString
}