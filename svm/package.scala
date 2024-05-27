import svm.base._
package object svm {
    import scribe._
    import scribe.file._
    import scribe.file.path.PathPart
    import spinal.core._
    import spinal.core.sim._
    import java.text.SimpleDateFormat
    import java.util.{Calendar, Date}
    type ValCallback = spinal.idslplugin.ValCallback
    type PostInitCallback = spinal.idslplugin.PostInitCallback
    type SvmComponentWrapper = SvmObjectWrapper[SvmComponent]
    
    val factory = SvmFactory
    implicit def objWrapperToObj[T <: SvmObject](objWrapper: SvmObjectWrapper[T]): T = objWrapper.getActualObj

    def getrealTime(pattern: String): String = {
        val timeTag = System.currentTimeMillis()
        val changeTime = new Date(timeTag)
        val dataFormat = new SimpleDateFormat(pattern)
        dataFormat.format(changeTime)
    }

    var _svmLogLevel = Level.Info
    var _svmLogFile = "logs/sim.log"
    def svmLogger = {
        val logger = Logger()
            .orphan()
            .withHandler()
            .withHandler(writer = FileWriter(_svmLogFile, append = false), formatter = scribe.format.Formatter.compact, outputFormat=scribe.output.format.ASCIIOutputFormat)
            .withMinimumLevel(_svmLogLevel)
        logger
    }
    
    def setSimLogFile(logfile: String): Unit = {
        _svmLogFile = logfile
    }
    
    val logger = svmLogger

    def setSvmLogLevel(level: String): Unit = {
        level.toLowerCase() match {
            case "low" => _svmLogLevel = Level.Trace
            case "medium" => _svmLogLevel = Level.Debug
            case "high" => _svmLogLevel = Level.Info
            case "warn" => _svmLogLevel = Level.Warn
            case "error" => _svmLogLevel = Level.Error
            case "fatal" => _svmLogLevel = Level.Fatal
            case _ => _svmLogLevel = Level.Info
        }
    }
    def svmLow(msg: String) = svmLogger.trace(f"@ ${simTime()} $msg")
    def svmMedium(msg: String) = svmLogger.debug(f"@ ${simTime()} $msg")
    def svmHigh(msg: String) = svmLogger.info(f"@ ${simTime()} $msg")
    def svmWarn(msg: String) = svmLogger.warn(f"@ ${simTime()} $msg")
    def svmError(msg: String) = svmLogger.error(f"@ ${simTime()} $msg")
    def svmFatal[E <: Exception](e: Exception)(msg: String) = {
        svmLogger.error(msg, e)
    }
    
}

