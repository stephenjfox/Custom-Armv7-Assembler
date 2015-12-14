package assembler

import com.fox.general.PredicateTests
import model.GlobalConfig
import model.Logger
import java.lang.Integer.parseInt
import java.lang.Integer.toBinaryString

/**
 * Home to all Token types.
 * Created by stephen on 11/28/15.
 */
abstract class Token(val content : String) {

    override fun toString() : String {
        return "${this.javaClass.simpleName}: ${this.content}"
    }
}

abstract class CommandToken(content : String) : Token(content) {
    abstract val conditionInt : Int
}

abstract class DataOperationCommandToken(content : String) : CommandToken(content) {
    abstract val setSBit : Boolean
}

class NewLineToken() : Token("\n\r")

class BranchCommand : CommandToken {
    override val conditionInt : Int

    constructor(content : String) : super(content) {
        val condition = content.substring(1)
        conditionInt = conditionCodeValue(condition)
    }
}

class BranchWithLinkCommand(content: String) : CommandToken(content) {
    override val conditionInt: Int

    init {
        val condition = content.substring(3)
        conditionInt = conditionCodeValue(condition)
    }
}

class MoveCommand : DataOperationCommandToken {
    override val conditionInt : Int
    val isMovT : Boolean
    val isMovW : Boolean
    override val setSBit : Boolean

    constructor(content : String) : super(content) {
        var finMovT = false
        var finMovW = false
        var finSetSBit = false
        var subStrInt = content.length
        when (content.length) {
            3, 4 -> {
                finMovT = content.last() == 'T'
                finMovW = content.last() == 'W'
                finSetSBit = content.last() == 'S'
            }
            5 -> {
                subStrInt = 3
            }
            6 -> {
                finSetSBit = content[3] == 'S'
                finMovT = content[3] == 'T'
                finMovW = content[3] == 'W'
                subStrInt = 4
            }
            else -> {
                Logger.e("Content passed into MOVEToken constructor was invalid")
                Logger.v("content = [$content]")

                throw IllegalArgumentException("Content argument was illegal length")
            }
        }

        isMovT = finMovT
        isMovW = finMovW
        setSBit = finSetSBit
        conditionInt = conditionCodeValue(content.substring(subStrInt))
    }
}

class LoadOperationToken : CommandToken {

    override val conditionInt : Int

    constructor(content : String) : super(content) {
        // scratch: this should only be LDR<c> as of Dec 1, 2015
        val substring = content.substring(3) // this cuts out the 'LDR'
        conditionInt = conditionCodeValue(substring)
    }
}

class RegisterToken : Token {

    val registerNumber : Int
    val nibble : String
        get() {
            var binaryString = toBinaryString(registerNumber)
            while (binaryString.length < 4) {
                // pad with zeroes
                binaryString = "0$binaryString"
            }
            return binaryString
        }

    constructor(content : String) : super(content) {
        PredicateTests.isTrue(content[0] == 'R')
        registerNumber = parseInt(content.substring(1)) // the '15' in 'R15'
    }

}

class ShiftToken : Token {

    val shiftAmount : Int

    constructor(content : String) : super(content) {
        // TODO: Support for the irregularly used 5-param functions
        // Ex being "A8.8.66 LDR (register, ARM)"
        shiftAmount = parseImmediateValue(content)
    }

}

class ImmediateToken(content : String) : Token(content) {
    val value : Int = parseImmediateValue(content)

    override fun toString() : String {
        return "${super.toString()}" + (if (GlobalConfig.getBoolean("verbose")) {
            " (Int = ${this.value})"
        } else "")
    }
}

class AddOperationToken : DataOperationCommandToken {
    override val setSBit : Boolean
    override val conditionInt : Int

    constructor(content : String) : super(content) {
        val pairOfData : Pair<Boolean, Int> = optionSCommandParse(content)

        setSBit = pairOfData.first
        conditionInt = pairOfData.second
    }
}

class SubtractOperationToken : DataOperationCommandToken {
    override val setSBit : Boolean
    override val conditionInt : Int

    constructor(content : String) : super(content) {
        val pairOfData : Pair<Boolean, Int> = optionSCommandParse(content)

        setSBit = pairOfData.first
        conditionInt = pairOfData.second
    }
}

class OrOperationToken : DataOperationCommandToken {
    override val setSBit : Boolean
    override val conditionInt : Int

    constructor(content : String) : super(content) {
        val pairOfData : Pair<Boolean, Int> = optionSCommandParse(content)

        setSBit = pairOfData.first
        conditionInt = pairOfData.second
    }
}

class StoreOperationToken(baseContent : String) : CommandToken(baseContent) {
    override val conditionInt : Int

    init {
        conditionInt = conditionCodeValue(baseContent.substring(3))
    }
}

class TypeToken(baseContent : String, val bitLimit : Int) : Token(baseContent) {

    init {
        if (baseContent.length != bitLimit) {
            error("TypeToken received too many bits: baseContent = [$baseContent], bitLimit = [$bitLimit]")
        }
    }
}

class PushManyToken(baseContent: String) : CommandToken(baseContent) {
    override val conditionInt: Int
    init {
        val pushString = baseContent.substring(4)
        conditionInt = conditionCodeValue(pushString)
    }
}

class PopManyToken(baseContent: String) : CommandToken(baseContent) {
    override val conditionInt: Int
    init {
        val substring = baseContent.substring(3)
        conditionInt = conditionCodeValue(substring)
    }
}

object Tokens {

    /**
     * Thinking back, these COULD be super types, with more specific types as
     * children. But then that defeats what I was trying to do with a flat
     * inheritance hierarchy
     */
    fun create(tokenType : TokenType, baseContent : String) : Token {

        Logger.v("Tokens.create() invoked with tokenType = $tokenType, baseContent = $baseContent")

        return when (tokenType) {
            TokenType.Register -> RegisterToken(baseContent)
            TokenType.Immediate -> ImmediateToken(baseContent)
            TokenType.Move -> MoveCommand(baseContent)
            TokenType.Branch -> BranchCommand(baseContent)
            TokenType.Shift -> ShiftToken(baseContent)
            TokenType.Load -> LoadOperationToken(baseContent)
            TokenType.NewLine -> NewLineToken()
            TokenType.Add -> AddOperationToken(baseContent)
            TokenType.Subtract -> SubtractOperationToken(baseContent)
            TokenType.Or -> OrOperationToken(baseContent)
            TokenType.Store -> StoreOperationToken(baseContent)
            TokenType.Type2Bit -> TypeToken(baseContent, 2) // should only have two bits
            TokenType.BranchWithLink -> BranchWithLinkCommand(baseContent)
            TokenType.Push -> PushManyToken(baseContent)// thing
            TokenType.Pop -> PopManyToken(baseContent)// other thing
            else -> object : Token("Dummy") {
                init {
                    Logger.v("Dummy made. this.javaClass = ${this.javaClass}")
                }
            }
        }
    }
}

enum class TokenType {
    Move, Load, Store, Add, Subtract, Branch, Register, Immediate, Or, Shift, NewLine,
    Type2Bit, BranchWithLink, Push, Pop
}