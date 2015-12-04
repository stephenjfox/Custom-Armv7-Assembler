package assembler

import kotlin.test.assertTrue
import org.junit.Test as test
/**
 * Tests relating to the Shift and Immediate Value tokens
 * 
 * Created by stephen on 12/3/15.
 */
class NumberValueTokenTest : BaseAssemblerTest() {

    @test fun immediateValueConstructorTest() {
        val immToken = Tokens.create(TokenType.Immediate, "0b1100")
        assertTrue { immToken is ImmediateToken }
        if (immToken is ImmediateToken) {
            assertTrue { immToken.value == 12 }
        }
    }
    
}