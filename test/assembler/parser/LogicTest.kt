package assembler.parser

import assembler.BaseAssemblerTest
import assembler.ImmediateToken
import assembler.TokenType
import assembler.Tokens
import model.not
import model.splitEvery
import model.toBinaryString
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test as test
/**
 * Created by stephen on 12/5/15.
 */
class LogicTest : BaseAssemblerTest() {

    @test fun paddingCheck_BasicTest() {
        val testBinary = "1001"
        val padded = paddingCheck(testBinary, 8)

        val split = padded.splitEvery(4)
        assertEquals("0000", split[0], "Should be padded with leading zeroes")
        assertEquals(testBinary, split[1], "Should be the trailing bits")
        assertTrue { padded.contains(testBinary) }
    }

    @test fun paddingCheck_DuplicateTest() {
        val testBinary = "1001"
        val padded = paddingCheck(testBinary, 4)

        val split = padded.splitEvery(4)
        assertEquals(1, split.size, "Split shouldn't be oversized")
        assertEquals(testBinary, padded, "These should be identical")
    }
    
    @test fun splitImmediate_FourAnd12Test() {
        val binaryString = paddingCheck(Integer.toBinaryString(Integer.parseInt("1FE2", 16)), 16)
        println("String = $binaryString, length = ${binaryString.length}")
        val (fourBits, twelveBits) = splitImmediateString(binaryString, 4, 12)

        assertEquals("0001", fourBits, "I want these bits padded as nibbles")
        assertEquals("111111100010", twelveBits, "These trailing bits should be equal")
    }

    @test fun buildRotatedEncodingsTest() {
        val immToken = Tokens.create(TokenType.Immediate, "0x200000") as ImmediateToken
        val hexAsInt = Integer.parseInt("200000", 16)

        assertEquals(expected = hexAsInt, actual = immToken.value, message = "These should be equivalent")

        println("0xff as Int = ${Integer.parseInt("ff", 16)}")
        println("0xff as binary = ${0xFF.toBinaryString()}")
        val notFF = Int.MAX_VALUE xor 0xff
        println("(Supposed) ~0xff as Int = ${notFF}")
        println("(Supposed) ~0xff as binary = ${notFF.toBinaryString()}")
        assertEquals(Int.MAX_VALUE - 255, notFF, "These are equal")
        assertEquals(notFF, not(0xff), "These should be equal")

        buildRotatedEncodings(0x200000).forEach { println(it) }
    }
}