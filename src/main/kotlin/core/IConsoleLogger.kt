package core

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Interface for console logger.
 */
interface IConsoleLogger {
    val logger: Logger
        get() = LoggerFactory.getLogger(javaClass.name)!!
}
