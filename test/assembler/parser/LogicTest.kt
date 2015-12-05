package assembler.parser

import assembler.BaseAssemblerTest
import model.splitEvery
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

}