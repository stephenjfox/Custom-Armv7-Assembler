package model

import com.fox.io.log.ConsoleLogger

/**
 * Created by stephen on 12/8/15.
 */
object Logger {
    fun d(message : String) {
        if (GlobalConfig.getBoolean("debug")) {
            ConsoleLogger.debug(message)
        }
    }

    fun v(obj : Any) {
        if (GlobalConfig.getBoolean("verbose")) {
            ConsoleLogger.writeLine(obj.toString())
        }
    }

    fun v(message : String) {
        if (GlobalConfig.getBoolean("verbose")) {
            ConsoleLogger.writeLine(message)
        }
    }

    fun w(message : String) {
        ConsoleLogger.warning(message)
    }

    fun e(errorMessage : String) {
        ConsoleLogger.error(errorMessage)
    }
}