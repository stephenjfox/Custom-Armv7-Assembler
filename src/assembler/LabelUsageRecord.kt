package assembler

/**
 * Created by stephen on 12/1/15.
 */
data class LabelUsageRecord(val word: String, var definitionLine: Int, val useLines: MutableCollection<Int>) {
}