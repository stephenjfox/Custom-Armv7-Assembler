package assembler

import org.junit.After
import org.junit.Before
import kotlin.test.expect
import org.junit.Test as test

/**
 * Created by stephen on 11/29/15.
 */
class TokenStreamCompanionTest {

    @Before fun setup() {
        expect(message = "List should be empty", expected = 0, block = { TokenStream.getRawYieldList().size })

        for (i in 1..3) TokenStream.yield(DummyToken("Dummy $i"))
    }

    @After fun tearDown() {
        TokenStream.getTokenStream()
    }

    @test fun willYieldTest() {
        expect(message = "Should be able to yield", expected = true, block =
        { TokenStream.willYield })

        TokenStream.yield(DummyToken())

        expect(message = "Should be able to yield", expected = true, block =
        { TokenStream.willYield })

        TokenStream.getTokenStream()

        expect(message = "Shouldn't be yielding", expected = false, block =
        { TokenStream.willYield })
    }

    @test fun getRawYieldList() {

        val yieldedList = TokenStream.getRawYieldList()

        expect(message = "List should be filled", expected = 3, block = { yieldedList.size })

        yieldedList.forEach { expect(message = "Everyone should be a Dummy",
                expected = true , block = { it is DummyToken }) }

    }

    @test fun getTokenStreamTest() {

        val resultantStream = TokenStream.getTokenStream()

        expect(message = "There should be three elements",
                expected = 3, block = { resultantStream.size })
    }
}
