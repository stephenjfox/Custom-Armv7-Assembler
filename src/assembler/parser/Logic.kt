package assembler.parser

import assembler.LoadCommand
import assembler.RegisterToken
import assembler.StoreOperationToken
import assembler.Token
import com.fox.io.log.ConsoleLogger
import model.ReversibleIterator
import model.empty
import model.size

/**
 * Created by stephen on 12/4/15.
 */
// returns the binary string of the operation encoding
fun loadParseLogic(loadToken : LoadCommand, iterator : ReversibleIterator<Token>) : String {
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

fun storeParseLogic(storeToken : StoreOperationToken, iterator : ReversibleIterator<Token>) : String {
    // TODO: fill in this logic
    return String.empty()
}