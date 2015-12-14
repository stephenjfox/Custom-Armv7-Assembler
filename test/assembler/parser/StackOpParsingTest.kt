package assembler.parser

import assembler.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import assembler.parser.parseTokenLine as parseToString

import org.junit.Test as test
/**
 * Created by Stephen on 12/13/2015.
 * In project: Custom-Armv7-Assembler
 */
class StackOpParsingTest : StackManyTokenTest() {

    private fun genPushToken(conditionString: String = "") : PushManyToken {
        return Tokens.create(TokenType.Push, "PUSH$conditionString") as PushManyToken
    }

    private fun genPopToken(conditionString: String = "") : PopManyToken {
        return Tokens.create(TokenType.Pop, "POP$conditionString") as PopManyToken
    }

    @test fun pushParseJustOne() {
        TokenStream.yield(genPushToken())
        val register = registers[0] // this is R1

        registers.removeIf { it.content == "R1" }
        assertTrue { registers.size == 4 }

        TokenStream.yield(register)
        val tokenStream = TokenStream.getTokenStream()

        val evaluatedString = parseToString(tokenStream)
        val correctCond = "1110"
        val staticBits = "100100101101"
        val registerStr = "0000000000000010" // I want this determination delegated to a function

        val testShouldBe = "$correctCond$staticBits$registerStr"
        println("testShouldBe.length = ${testShouldBe.length}")
        assertEquals(testShouldBe, evaluatedString,
                "This implementation may not be correct")
    }

    @test fun popParseJustOne() {
        TokenStream.yield(genPopToken())
        val register = registers[0] // this is R1

        registers.remove(register)
        assertTrue { registers.size == 4 }

        TokenStream.yieldSequential(registers)
        val tokenStream = TokenStream.getTokenStream()

        val evaluatedString = parseToString(tokenStream)
        val correctCond = "1110"
        val staticBits = "100010111101"
        // R1 one was removed
        val registerStr = "0000000000111100" // I want this determination delegated to a function

        val testShouldBe = "$correctCond$staticBits$registerStr"
        println("testShouldBe.length = ${testShouldBe.length}")
        assertEquals(testShouldBe, evaluatedString,
                "This implementation may not be correct")
    }
}