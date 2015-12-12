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
        else {
            val list = ArrayList<Int>()
            list.add(lineNumber)
            symbolMap[name] = LabelUsageRecord(name, -1, list)
        }
        Logger.d("symbolMap = $symbolMap")
    }

    fun putDefinitionSite(name : String, lineNumber : Int) {
        Logger.v("LabelUsageMap -> putDefinitionSite")
        Logger.d("name(key) = [$name], lineNumber = [$lineNumber]")
        val labelUsageRecord = symbolMap[name]
        if (labelUsageRecord == null) {
            symbolMap[name] = LabelUsageRecord(name, lineNumber, ArrayList())
        }
        else {
            symbolMap[name]!!.definitionLine = lineNumber
        }
        Logger.d("symbolMap = $symbolMap")
    }

}