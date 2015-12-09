package assembler

import model.Logger
import java.util.*

class LabelUsageMap(val symbolMap : MutableMap<String, LabelUsageRecord> = HashMap<String, LabelUsageRecord>())
: Map<String, LabelUsageRecord> by symbolMap {

    fun putUsageSite(name : String, lineNumber : Int) {
        Logger.v("LabelUsageMap -> putUsageSite")
        Logger.d("name = [$name], lineNumber = [$lineNumber]")
        val usageRecord = symbolMap[name]
        if (usageRecord != null) {
            usageRecord.useLines.add(lineNumber)
            symbolMap[name] = usageRecord
        }
        Logger.d("symbolMap = $symbolMap")
    }

    fun putDefinitionSite(name : String, lineNumber : Int) {
        Logger.v("LabelUsageMap -> putDefinitionSite")
        Logger.d("name(key) = [$name], lineNumber = [$lineNumber]")
        symbolMap[name] = LabelUsageRecord(name, lineNumber, ArrayList())
        Logger.d("symbolMap = $symbolMap")
    }

}