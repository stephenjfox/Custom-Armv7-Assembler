package assembler

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test as test
/**
 * Created by stephen on 11/29/15.
 */
class TokenStreamTest {

    private var tokenStream : TokenStream = TokenStream.emptyStream

    @Before fun setup() {
        // init the tokenStream with something worthwhile
        tokenStream = TokenStream.of(DummyToken(), DummyToken(), DummyToken())
    }

    @After fun tearDown() {
        TokenStream.getTokenStream()
    }

    @test fun iteratorExistenceTest() {
        Assert.assertNotNull("This is definitely not be null", tokenStream.iterator())
    }

    /**
     * For when I want to fetch two iterators, they shouldn't conflict.
     * But I should have two lens at the same Iterable
     */
    @test fun iteratorSimultaneityTest() {
        val first = tokenStream.iterator()
        val second = tokenStream.iterator()

        Assert.assertNotEquals("These streams shouldn't be identical",
                first, second)

        Assert.assertEquals("Neither should be able to reverse",
                first.canReverseNext(), second.canReverseNext())
        Assert.assertEquals("Both should be able to next()",
                first.hasNext(), second.hasNext())

        if (first.hasNext() and second.hasNext()) {
            Assert.assertEquals("The elements, however, should be equal",
                    first.next(),
                    second.next())
        }
    }

}