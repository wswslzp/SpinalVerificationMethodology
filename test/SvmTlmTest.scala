import svm._
import svm.tlm._
import spinal.core._
import spinal.core.sim._

object SvmTlmTest extends App {
    
    SimConfig.withIVerilog.compile(SvmRtlForTest()).doSim({ dut =>
        dut.clockDomain.forkStimulus(10)
    })
}