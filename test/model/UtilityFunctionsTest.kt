package model

import kotlin.test.assertEquals
import org.junit.Test as test
/**
 * Created by stephen on 12/4/15.
 */
class UtilityFunctionsTest {

    @test fun splitEveryTest() {
        val testString = "E6923500"
        val splitEvery = testString.splitEvery(2)
        assertEquals(message = "splitEverything.length should equal ${testString.length}/2",
                expected = 4, actual = splitEvery.size)
        assertEquals("E6", splitEvery[0], "The split should produce proper subStrings")
        assertEquals("92", splitEvery[1])
        val reverse = splitEvery.reversedArray()
        assertEquals(splitEvery[0], reverse[3], "The reverse function is tested and works")
    }

}