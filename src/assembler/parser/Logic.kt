package assembler.parser

import assembler.*
import com.fox.general.PredicateTests.isTrue
import com.fox.io.log.ConsoleLogger
import model.GlobalConfig
import model.ReversibleIterator
import model.size
import java.util.*

/**
 * The logical functions that each handle a specific type of command: MOV(W/T/S), SUB, LDR, etc.
 *
 * These are not aggregated into a class, because that classes sole purpose would be to group these
 * functions. The behaviors are associated only in that they work on similar data. Thus, they are
 * in the same file.
 *
 * Created by stephen on 12/4/15.
 */

fun moveParseLogic(moveToken : MoveCommand, iterator : ReversibleIterator<Token>) : String {

    var registerBit = '1' // is an immediate value. BIT 25
    val iteratorSize = iterator.size()
    val condition = Integer.toBinaryString(moveToken.conditionInt)
    val sBit = (if (moveToken.setSBit) '1' else '0')

    val builder = StringBuilder(32)

    if (GlobalConfig.getBoolean("verbose")) {
        ConsoleLogger.debug("Iterator.size = $iteratorSize")
    }

    // TODO: refactor out the repeated code blocks
    when (iteratorSize) {
        3 -> {
            val registerDest = iterator.next() as RegisterToken
            val immAtLeast12Token = iterator.next() as ImmediateToken
            val staticBits = ("00${registerBit}1" + (
                    if (!(moveToken.isMovT || moveToken.isMovW)) "101$sBit" // MOV(S)
                    else if (moveToken.isMovW) "0000" // MOVW
                    else "0100") /*MOVT*/)
            val immBinString = paddingCheck(Integer.toBinaryString(immAtLeast12Token.value), 16)
            val (imm4, imm12) = splitImmediateString(immBinString, 4, 12)
            // this is the order of the bits for the move command.
            builder.append(condition)
                    .append(staticBits)
                    .append(imm4)
                    .append(registerDest.nibble)
                    .append(imm12)
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

/**
 * Takes in a Load or Store [DataOperationCommandToken], along with the rest of tokens that represent its
 * parameters.
 *
 * throw [IllegalStateException] if the passed arguments manage to be neither [LoadOperationToken] nor
 * [StoreOperationToken]
 */
fun loadStoreParseLogic(ldrStrToken : CommandToken, iterator : ReversibleIterator<Token>) : String {

    isTrue(ldrStrToken is LoadOperationToken || ldrStrToken is StoreOperationToken)

    val builder = StringBuilder(32)
    var PUWTriple = Triple('1', '1', '0') // Bits P, U, and W saying don't PUSHback, add UP and don't WRITEback

    val iteratorSize = iterator.size()
    var rBit = '0' // is working with a register for the 'value' that will be operated on
    val identBit = (if (ldrStrToken is LoadOperationToken) 1 else 0)
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
            // TODO: generate using the modified immediate constants, bit rotation on pg. 200/2734
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
        var registerBit = '0' // TODO: change this bit later - when using registers for values
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

fun orOperationParse(orToken : OrOperationToken, iterator : ReversibleIterator<Token>) : String {
    // things go here
    val builder = StringBuilder(32)

    val condition = Integer.toBinaryString(orToken.conditionInt)
    val registerDest = iterator.next() as RegisterToken
    val registerSource = iterator.next() as RegisterToken
    val sBit = (if (orToken.setSBit) '1' else '0')
    val staticBits = "0011100$sBit"

    when (iterator.size()) {
        4 -> {
            // ORR{S}<c> <Rd>, <Rn>, #<const>
            val immToken = iterator.next() as ImmediateToken
            // now to deal with numbers that are too big
            val fixedTokenVal = modifiedConstantCheck(immToken)

            builder.append(condition)
                    .append(staticBits)
                    .append(registerSource.nibble)
                    .append(registerDest.nibble)
                    .append(fixedTokenVal)
        }
        5 -> {
            // ORR{S}<c> <Rd>, <Rn>, <Rm>{, <shift>}
        }
        6 -> {
            // ORR{S}<c> <Rd>, <Rn>, <Rm>, <type> <Rs>
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

fun modifiedConstantCheck(immToken : ImmediateToken) : String {
    // make a 8 bit value turn into a 32 bit value, with 4 bits for rotation
    isTrue(immToken.value > 0) // if the int flipped, we've got another problem
    // TODO: deal with immToken binary longer than bitCapacity
//    Integer.rotateRight()
    throw UnsupportedOperationException("not implemented")
}

fun splitImmediateString(binary : String, vararg chunkSizes : Int) : List<String> {
    isTrue(binary.length <= chunkSizes.sum())

    var currentIndex = 0
    val retList = ArrayList<String>()

    for (chunkSize in chunkSizes) {
        val strToAdd = binary.substring(currentIndex, chunkSize + currentIndex)
        currentIndex += chunkSize
        retList += strToAdd
    }

    if (currentIndex != binary.lastIndex) retList += binary.substring(currentIndex)

    return retList
}
