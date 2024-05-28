import scribe._
import scribe.file._
import svm._

object SvmLoggingTest extends App {

  // var logger = Logger()
  //   .orphan()
  //   .withHandler(writer = FileWriter("logs/test1.log", append = false), formatter = scribe.format.Formatter.advanced, outputFormat=scribe.output.format.ASCIIOutputFormat)
  //   .withHandler()
  //   .withMinimumLevel(Level.Trace)

  for (i <- 1 to 10) {
    logger.info(f" Num $i")
  }
  setSvmLogLevel("low")
  for (i <- 1 to 10) {
    logger.trace(f" Num $i")
  }

  // println(f"logger.handlers.size = ${logger.handlers.size}, logger.modifiers.size = ${logger.modifiers.size}")
  // logger.handlers.foreach(hdl => println(hdl.toString()))
  // logger.modifiers.foreach(mod => println(mod.toString()))

  // SimConfig.withIVerilog.withWave.compile(SvmRtlForTest()).doSim({ dut =>
  //     dut.clockDomain.forkStimulus(10 ns)
  //     val timestamp = getrealTime("yyyyMMddHHmmss")
  //     println(timestamp)
  //     svmLogLevel("low")
  //     svmLow("LOWLOW")
  //     svmMedium("MEIDUM")
  //     svmHigh("HIGH")
  //     svmWarn("WARN")
  // })
}
