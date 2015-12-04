package assembler

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail
import org.junit.Test as test
/**
 * NOTE: ADD, SUB, and ORR all use the same parent class, with no uniqueness
 * among themselves, same their names. This is using INHERITANCE for
 * disambiguation, because it's something that is reliable in my book.
 * THEREFORE, throughout these tests I will be alternating between the three
 * TokenTypes on a whim, because it should have no effect on the test
 *
 * Created by stephen on 12/3/15.
 */
class NumericDataOperationTokenTest : BaseAssemblerTest() {

    @test fun basicConstructionTest() {
        val token = Tokens.create(TokenType.Add, "ADD")

        assertTrue(token is AddOperationToken, "This should make an ADD token")

        if (token is AddOperationToken) {
            // things
            assertFalse(token.setSBit, "The S-bit should be off")
            assertEquals(14, token.conditionInt,
                    "The condition should be 'always' or 0b1110")
        }
        else {
            fail("Token should be an instance of AddOperationToken")
        }
    }

    @test fun basicSConstructorTest() {
        val token = Tokens.create(TokenType.Add, "ADDS")

        assertTrue(token is AddOperationToken, "This should still be an ADD op token")

        if (token is AddOperationToken) {
            println("token = $token")
            assertTrue(token.setSBit, "The S-bit should be on")
            assertEquals(14, token.conditionInt,
                    "Should be token code 'always' 0b1110")
        }
        else {
            fail("Token should be an instance of AddOperationToken")
        }
    }

    @test fun conditionConstructorTest() {
        val token = Tokens.create(TokenType.Or, "ORRNE")

        assertTrue(token is OrOperationToken, "The token should be na 'ORR' token")

        if (token is OrOperationToken) {
            println("token = $token")
            assertFalse(token.setSBit, "The S-bit should be off")
            assertEquals(1, token.conditionInt, "Should be set to NE (0b0001)")
        }
    }

    @test fun conditionSConstructorTest() {
        val token = Tokens.create(TokenType.Subtract, "SUBSHI")

        assertTrue(token is SubtractOperationToken, "The token should be a subtraction token")

        if (token is SubtractOperationToken) {
            println("token = $token")
            assertTrue(token.setSBit, "The S-bit should be on")
            assertEquals(8, token.conditionInt, "Should be set to HI (0b1000)")
        }
    }

}