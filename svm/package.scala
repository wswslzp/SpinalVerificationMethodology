import svm.base._
package object svm {
    import scribe._
    import scribe.format._
    import scribe.file._
    import scribe.file.path.PathPart
    import perfolation._
    import scribe.data.MDC
    import scribe.output.{CompositeOutput, EmptyOutput, LogOutput, TextOutput}
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

    object SvmSimTime extends FormatBlock {
        override def format[M](record: LogRecord[M]): LogOutput = new TextOutput(s"${simTime().toString()}")
    }
    var _svmLogLevel = Level.Info
    var _svmLogFile = "logs/sim.log"
    lazy val svmCompatFormat: Formatter = formatter"SVM ${string("[")}$levelColored${string("]")} ${green(position)} @${SvmSimTime} $message$mdc"
    def logger = {
        val _logger = Logger()
            .orphan()
            .withHandler()
            .withHandler(writer = FileWriter(_svmLogFile, append = false), formatter = svmCompatFormat, outputFormat=scribe.output.format.ASCIIOutputFormat)
            .withMinimumLevel(_svmLogLevel)
        _logger
    }
    
    def setSimLogFile(logfile: String): Unit = {
        _svmLogFile = logfile
    }
    
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
}

