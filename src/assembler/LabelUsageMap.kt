package assembler

import model.GlobalConfig
import java.util.*

class LabelUsageMap {

    private val symbolMap = HashMap<String, LabelUsageRecord>()

    fun putUsageSite(name : String, lineNumber : Int) {
        if (GlobalConfig.getBoolean("verbose")) {
            println("LabelUsageMap -> putUsageSite")
        }
        if (GlobalConfig.getBoolean("debug")) {
            println("name = [${name}], lineNumber = [${lineNumber}]")
            println("symbolMap = ${symbolMap}")
        }
    }

}