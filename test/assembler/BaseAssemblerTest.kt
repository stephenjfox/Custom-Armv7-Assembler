package assembler

import model.GlobalConfig
import org.junit.Before

/**
 * Created by stephen on 12/3/15.
 */
abstract class BaseAssemblerTest {
    @Before open fun setup() {
        GlobalConfig.initDefaults()
    }
}