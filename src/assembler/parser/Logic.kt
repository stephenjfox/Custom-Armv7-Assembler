package assembler.parser

import assembler.CommandToken
import assembler.LoadCommand
import assembler.RegisterToken
import assembler.StoreOperationToken
import assembler.Token
import com.fox.general.PredicateTests.isTrue
import com.fox.io.log.ConsoleLogger
import model.GlobalConfig
import model.ReversibleIterator
import model.size

/**
 * Created by stephen on 12/4/15.
 */
// returns the binary string of the operation encoding
fun loadParseLogic(loadToken : LoadCommand, iterator : ReversibleIterator<Token>) : String {
    val builder = StringBuilder(32)

    var PUWTriple = Triple('1', '1', '0') // Bits P, U, and W saying don't PUSHback, add UP and don't WRITEback

    val iteratorSize = iterator.size()
    ConsoleLogger.debug("Iterator.size = $iteratorSize")
    when (iteratorSize) {
        3 -> {
            // treat like a 4-token instruction, with '0' for an immediate value
            val registerDest = iterator.next() as RegisterToken
            val registerSource = iterator.next() as RegisterToken
            // FIXME: the last '1' here is what toggles between 'STR' and 'LDR'. DECOUPLE
            val immOpCode = "010" + PUWTriple.first + PUWTriple.second + '0' + PUWTriple.third + '1'
            // FIXME: a 0_ here^ means immediate value
            builder.append(Integer.toBinaryString(loadToken.conditionInt))
            builder.append(immOpCode)
            builder.append(registerSource.nibble)
            builder.append(registerDest.nibble)
            // pad the last imm12 bits with 0s
            builder.append("000000000000")
        }
    }
    val toString = builder.toString()

    if (GlobalConfig.getBoolean("debug")) {
        val binAsInt = java.lang.Long.parseLong(toString, 2)
        val hexString = java.lang.Long.toHexString(binAsInt)
        ConsoleLogger.debug("Built binary $toString. Hex: $hexString")
    }

    return toString
}

fun storeParseLogic(storeToken : StoreOperationToken, iterator : ReversibleIterator<Token>) : String {
    // TODO: fill in this logic
    val builder = StringBuilder(32)

    var PUWTriple = Triple('1', '1', '0') // Bits P, U, and W saying don't PUSHback, add UP and don't WRITEback

    val iteratorSize = iterator.size()
    ConsoleLogger.debug("Iterator.size = $iteratorSize")
    when (iteratorSize) {
        3 -> {
            // treat like a 4-token instruction, with '0' for an immediate value
            val registerDest = iterator.next() as RegisterToken
            val registerSource = iterator.next() as RegisterToken
            // FIXME: the last '0' here is what toggles between 'STR' and 'LDR'. DECOUPLE
            val immOpCode = "010" + PUWTriple.first + PUWTriple.second + '0' + PUWTriple.third + '0'
            builder.append(Integer.toBinaryString(storeToken.conditionInt))
            builder.append(immOpCode)
            builder.append(registerSource.nibble)
            builder.append(registerDest.nibble)
            // pad the last imm12 bits with 0s
            builder.append("000000000000")
        }
    }
    val toString = builder.toString()
    return toString
}

fun loadStoreParseLogic(ldrStrToken : CommandToken, iterator : ReversibleIterator<Token>) : String {

    isTrue(ldrStrToken is LoadCommand || ldrStrToken is StoreOperationToken)

    val builder = StringBuilder(32)
    var PUWTriple = Triple('1', '1', '0') // Bits P, U, and W saying don't PUSHback, add UP and don't WRITEback

    val iteratorSize = iterator.size()
    var rBit = '0' // is working with a register for the 'value' that will be operated on
    val identBit = (if (ldrStrToken is LoadCommand) 1 else 0)
    val bit22 = '0'

    if (GlobalConfig.getBoolean("verbose")) {
        ConsoleLogger.debug("Iterator.size = $iteratorSize")
    }
    when (iteratorSize) {
        3 -> {
            // treat like a 4-token instruction, with '0' for an immediate value
            val registerDest = iterator.next() as RegisterToken
            val registerSource = iterator.next() as RegisterToken
            val immOpCode = "01" + rBit + PUWTriple.first + PUWTriple.second + bit22 + PUWTriple.third + identBit
            builder.append(Integer.toBinaryString(ldrStrToken.conditionInt))
                    .append(immOpCode)
                    .append(registerSource.nibble)
                    .append(registerDest.nibble)
            // pad the last imm12 bits with 0s
            builder.append("000000000000")
        }
        4 -> {
            // TODO: using the modified immediate constants, right rotation on pg. 200/2734
        }
    }
    val toString = builder.toString()

    if (GlobalConfig.getBoolean("debug")) {
        val binAsInt = java.lang.Long.parseLong(toString, 2)
        val hexString = java.lang.Long.toHexString(binAsInt)
        ConsoleLogger.debug("Built binary $toString. Hex: $hexString")
    }

    return toString
}