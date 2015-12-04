package model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import kotlin.test.expect
import org.junit.Test as test
/**
 * Created by stephen on 11/28/15.
 */
class ReversibleIteratorTest {

    var iterator : ReversibleIntIterator = ReversibleIntIterator(emptyList())

    @Before fun setup() {
        val srcList = listOf(1, 2, 3, 4, 5)
        iterator = ReversibleIntIterator(srcList)
    }
    
    @test fun nextTest() {

        val builder = StringBuilder()
        while (iterator.hasNext()) {
            builder.append(iterator.next())
        }

        val parseInt = Integer.parseInt(builder.toString())

        assertEquals("There should be a long number", 12345, parseInt)

        try {
            iterator.next()
        } catch (ex : Exception) {
            assertTrue("You've made it to the success point. Why you fail", true)
        }
    }

    @test fun skipToEnd() {
        iterator.skipToEnd()

        try {
            iterator.next()
        } catch (ex : Exception) {
            assertTrue("You've made it to the success point. Why you fail", true)
        }
    }

    @test fun reverseNextTest() {
        iterator.skipToEnd()

        assertEquals("This should return the last thing that could be iterated", 5, iterator.reverseNext())

        expect(expected = 4, block = { iterator.reverseNext() }, message = "Should still be viewing the previous")
    }

    @test fun reverseNextTest2() {
        iterator.skipToEnd()

        val builder = StringBuilder()
        while (iterator.canReverseNext()) {
            builder.append(iterator.reverseNext())
        }

        val parseInt = Integer.parseInt(builder.toString())

        expect(message = "Parse should read the same", expected = 54321, block = { parseInt })

    }

}
