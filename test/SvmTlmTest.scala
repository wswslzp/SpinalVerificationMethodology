import svm._
import svm.base._
import svm.tlm._
import svm.comps._
import spinal.core._
import spinal.core.sim._
import svm.seq.SvmSequencer

object SvmTlmTest extends App {
    SimConfig.withIVerilog.withWave.compile(SvmRtlForTest()).doSim({ dut =>
        dut.clockDomain.forkStimulus(10 ns)
        setSvmLogLevel("low")
        setSimLogFile("SvmTlmTest.log")
        SvmRunTest(dut, new A_test())
    })
}