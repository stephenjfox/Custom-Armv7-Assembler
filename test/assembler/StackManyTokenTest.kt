package assembler

import org.junit.Before
import java.util.*
import kotlin.test.assertTrue
import org.junit.Test as test
/**
 * Created by Stephen on 12/13/2015.
 * In project: Custom-Armv7-Assembler
 */
class StackManyTokenTest : BaseAssemblerTest() {

    val registers = ArrayList<Token>()

    @Before override fun setup() {
        super.setup()
        "R1 R2 R3 R4 R5".split(" ").forEach { 
            registers.add(Tokens.create(TokenType.Register, it))
        }
    }
    
    @test fun basicConstructionTest() {
        val pushToken = Tokens.create(TokenType.Push, "PUSH")
        val popToken = Tokens.create(TokenType.Pop, "POP")

        assertTrue(pushToken is PushManyToken)
        assertTrue(popToken is PopManyToken)

        TokenStream.yield(popToken)
        TokenStream.yieldSequential(registers)
        val tokenStream = TokenStream.getTokenStream()

        assert(tokenStream.size == 6)
        val iterator = tokenStream.iterator()
        assertTrue { iterator.next()
            iterator.next() is RegisterToken
        }
    }
    
    @test fun conditionalConstructionTest() {
        val makePushToken = Tokens.create(TokenType.Push, "PUSHCC")
        val makePopToken = Tokens.create(TokenType.Pop, "POPLE")

        assertTrue(makePushToken is PushManyToken)
        assertTrue(makePopToken is PopManyToken)

        val pushToken = makePushToken as PushManyToken
        val popToken = makePopToken as PopManyToken

        assertTrue(pushToken.conditionInt == 3)
        assertTrue(popToken.conditionInt == 13)
    }
}