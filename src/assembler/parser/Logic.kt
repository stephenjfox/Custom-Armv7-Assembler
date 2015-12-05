package assembler.parser

import assembler.AddOperationToken
import assembler.CommandToken
import assembler.DataOperationCommandToken
import assembler.ImmediateToken
import assembler.LoadCommand
import assembler.RegisterToken
import assembler.StoreOperationToken
import assembler.SubtractOperationToken
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

fun addSubOperationParse(addSubToken : DataOperationCommandToken, iterator : ReversibleIterator<Token>) : String {
    // no PUW bits this time 'round.
    if (addSubToken is AddOperationToken || addSubToken is SubtractOperationToken) {

        val builder = StringBuilder()
        val iteratorSize = iterator.size()
        var registerBit = '0' // todo: use this thing later
        val idPair = (if (addSubToken is AddOperationToken) "10" else "01")
        val sBit = (if (addSubToken.setSBit) '1' else '0')
        val registerDest = iterator.next() as RegisterToken
        val registerSource = iterator.next() as RegisterToken

        when (iteratorSize) {
            4 -> {
                registerBit = '1' // in this case, '1' means immediate. Because ARM
                val imm12Token = iterator.next() as ImmediateToken
                // We're doing the immediate ADD/SUB
                val staticBits = "00" + registerBit + '0' + idPair + '0' + sBit
                builder.append(Integer.toBinaryString(addSubToken.conditionInt))
                        .append(staticBits)
                        .append(registerSource.nibble)
                        .append(registerDest.nibble)

                val immBinary = Integer.toBinaryString(imm12Token.value)

                builder.append(paddingCheck(immBinary, 12))
            }
        }

        val toString = builder.toString()

        if (GlobalConfig.getBoolean("debug")) {
            val binAsInt = java.lang.Long.parseLong(toString, 2)
            val hexString = java.lang.Long.toHexString(binAsInt)
            ConsoleLogger.debug("Built binary $toString. Hex: $hexString")
        }

        return toString
    } else {
        throw IllegalArgumentException("addSubToken parameter was not valid: $addSubToken")
    }
}

fun paddingCheck(binary : String, capacity : Int) : String {
    var returnStr = (if (binary.length > capacity) {
        // assume "Modified Immediate Constants"
        binary.substring(binary.length - capacity)
    } else {
        // pad with zeros
        val shortBy = capacity - binary.length
        "0".repeat(shortBy) + binary
    })
    return returnStr
}
