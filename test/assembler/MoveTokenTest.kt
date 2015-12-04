package assembler

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlin.test.assertEquals
import org.junit.Test as test
/**
 * Created by stephen on 11/30/15.
 */

class MoveTokenTest : BaseAssemblerTest() {

    @test fun basicConstructorTest() {

        val token = Tokens.create(TokenType.Move, "MOV")

        assertTrue("Should be a move token", token is MoveCommand)
        if (token is MoveCommand) {
            assertEquals(14, token.conditionInt,
                    "Should be 0b1110 or 0d14")
            assertFalse("Should NOT be an S bit", token.setSBit)
            assertFalse("Should NOT be a MOVW", token.isMovW)
            assertFalse("Should NOT be a MOVT", token.isMovT)
        }
    }

    @test fun basicMovWConstructorTest() {
        val token = Tokens.create(TokenType.Move, "MOVW")
        assertTrue(token is MoveCommand)
        if (token is MoveCommand) {
            assertTrue("Token is movW, so we should see a 'W'", token.isMovW)
            assertFalse("This is not a MOVT", token.isMovT)
            assertFalse("MOVW's don't use the S-bit", token.setSBit)
        }
    }

    @test fun basicMovTConstructorTest() {
        val token = Tokens.create(TokenType.Move, "MOVT")
        assertTrue(token is MoveCommand)
        if (token is MoveCommand) {
            assertTrue("Token is movT, so we should see a 'T'", token.isMovT)
            assertFalse("This is not a MOVW", token.isMovW)
            assertFalse("MOVT's don't use the S-bit", token.setSBit)
        }
    }


    @test fun conditionalMovConstructorTest() {
        val token = Tokens.create(TokenType.Move, "MOVNE")
        assertTrue(token is MoveCommand)
        if (token is MoveCommand) {
            assertFalse("Shouldn't have a 'T' set", token.isMovT)
            assertFalse("Shouldn't have a 'W' set", token.isMovW)
            assertFalse("Shouldn't have a 'S'-bit", token.setSBit)

            assertEquals(message = "NE = 0x0001. token.conditionInt == 1",
                     expected = 1, actual = token.conditionInt)
        }
    }

    @test fun conditionalMovWConstructorTest() {
        val token = Tokens.create(TokenType.Move, "MOVWCC")
        assertTrue(token is MoveCommand)

        if (token is MoveCommand) {
            assertTrue("Is a MOVW bit", token.isMovW)
            assertFalse(token.isMovT)
            assertFalse(token.setSBit)

            assertEquals(message = "CC = 0011. token.conditionInt == 3",
                    expected = 3, actual = token.conditionInt)
        }
    }


    @test fun conditionalMovTConstructorTest() {
        val token = Tokens.create(TokenType.Move, "MOVTGT")
        assertTrue(token is MoveCommand)

        if (token is MoveCommand) {
            assertTrue("Is a MOVT bit", token.isMovT)
            assertFalse(token.isMovW)
            assertFalse(token.setSBit)

            assertEquals(message = "GT = 1100. token.conditionInt == 12",
                    expected = 12, actual = token.conditionInt)
        }
    }

    @test fun conditionalMovSBitConstructorTest() {
        val token = Tokens.create(TokenType.Move, "MOVSNE")
        assertTrue(token is MoveCommand)

        if (token is MoveCommand) {
            assertTrue("S-bit should be set", token.setSBit)
            assertFalse("NOT a MovW", token.isMovW)
            assertFalse("NOT a MovT", token.isMovT)

            assertEquals(1, token.conditionInt,
                    "token.conditionalInt should equal '1'")
        }
    }
}