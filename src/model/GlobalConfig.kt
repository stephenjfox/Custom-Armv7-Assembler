package model

import java.util.*

/**
 * Created by stephen on 11/29/15.
 */
object GlobalConfig {

    private val propertyMap = HashMap<String, Any>()

    fun initDefaults() {
        setProperty("verbose", false)
        setProperty("debug", false)
        setProperty("info", false)
    }

    fun setProperty(property : String, value : Boolean) {
        propertyMap[property] = value
        if (propertyMap["verbose"] as Boolean) {
            propertyMap["debug"] = true
        }
    }

    fun get(key : String) : Any? = propertyMap[key]

    fun getBoolean(key: String) : Boolean {
        if (propertyMap.containsKey(key))
            return propertyMap[key] as Boolean
        throw PropertyDoesNotExistException("property name")
    }

    class PropertyDoesNotExistException(missingProperty : String) :
    Exception("Property $missingProperty wasn't set in the map: $propertyMap}")

}
