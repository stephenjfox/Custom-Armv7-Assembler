package assembler

import com.fox.general.IntegerExtension
import model.Logger.d
import model.Logger.v
import java.util.*

/**
 * Created by stephen on 11/30/15.
 */
fun conditionCodeValue(conditionName : String) : Int {
    val ret = when (conditionName.toUpperCase()) {
        "EQ" -> 0
        "NE" -> 1
        "CS" -> 2
        "CC" -> 3
        "MI" -> 4
        "PL" -> 5
        "VS" -> 6
        "VC" -> 7
        "HI" -> 8
        "LS" -> 9
        "GE" -> 10
        "LT" -> 11
        "GT" -> 12
        "LE" -> 13
        "" -> 14 // 'Always', this might not need to be here
        "HS" -> conditionCodeValue("CS")
        "LO" -> conditionCodeValue("CC")
        else -> -1
    }
    d("Ret = $ret, with conditionName = [$conditionName]")
    return ret
}

fun parseImmediateValue(string : String) : Int {

    v("TokenUtils -> parseImmediateValue")
    d("immediate string parse = [$string]")

    val tryParse = IntegerExtension.tryParse(string)

    if (tryParse.first) {
        return tryParse.second
    }

    val subContent = string.substring(2) // the '3F20' in '0x3F20'
    return when (string[1]) {
        'b' -> Integer.parseInt(subContent, 2)
        'x' -> Integer.parseInt(subContent, 16)
        'o' -> Integer.parseInt(subContent, 8)
        else -> Integer.parseInt(string) // this case shouldn't actually be reached
    }
}

fun optionSCommandParse(optionalCommandString : String) : Pair<Boolean, Int> {
    val settingS : Boolean
    val finalConditionInt : Int
    when (optionalCommandString.length) {
        3, 4 -> {
            settingS = optionalCommandString.last() == 'S'
            finalConditionInt = conditionCodeValue("")
        }
        5 -> {
            settingS = false // ORR<c>optionalCommandString[3]
            finalConditionInt = conditionCodeValue(optionalCommandString.substring(3))
        }
        6 -> {
            settingS = optionalCommandString[3] == 'S'
            finalConditionInt = conditionCodeValue(optionalCommandString.substring(4))
        }
        else ->
            throw IllegalStateException("ORR Token initialization failed with args: optionalCommandString = $optionalCommandString")
    }

    return Pair(settingS, finalConditionInt)
}

fun TokenStream.spliceLines() : List<TokenStream> {
    var currentStreamIndex = 0
    val streamList = ArrayList<ArrayList<Token>>()

    streamList.add(ArrayList())

    for (token in this) {
        if (token is NewLineToken) {
            streamList.add(ArrayList())
            currentStreamIndex += 1
        } else {
            streamList[currentStreamIndex].add(token)
        }
    }

    return streamList.map { TokenStream(it) }
}