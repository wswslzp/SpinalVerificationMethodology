package object svm {
    import org.log4s.getLogger
    import org.log4s.LogLevel
    import spinal.core._
    import spinal.core.sim._
    
    type ValCallback = spinal.idslplugin.ValCallback
    type PostInitCallback = spinal.idslplugin.PostInitCallback
    
    def svmLogger = {
        val logger = getLogger("SVM")
        logger
    }
    def svmLogLevel(level: String): Unit = {
        level.toLowerCase() match {
            case "low" => svmLogger(LogLevel.forName("trace"))
            case "medium" => svmLogger(LogLevel.forName("debug"))
            case "high" => svmLogger(LogLevel.forName("info"))
            case "warn" => svmLogger(LogLevel.forName("warn"))
            case "error" => svmLogger(LogLevel.forName("error"))
            case _ => svmLogger(LogLevel.forName("info"))
        }
    }
    def svmLow(msg: String) = svmLogger.trace(f"@ ${simTime()} $msg")
    def svmMedium(msg: String) = svmLogger.debug(f"@ ${simTime()} $msg")
    def svmHigh(msg: String) = svmLogger.info(f"@ ${simTime()} $msg")
    def svmWarn(msg: String) = svmLogger.warn(f"@ ${simTime()} $msg")
    def svmError(msg: String) = svmLogger.error(f"@ ${simTime()} $msg")
    def svmFatal[E <: Exception](e: Exception)(msg: String) = svmLogger.error(e)(f"@ ${simTime()} $msg")
    
}

