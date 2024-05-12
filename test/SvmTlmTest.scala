import svm._
import svm.base._
import svm.tlm._
import svm.comps._
import spinal.core._
import spinal.core.sim._

case class txn() extends SvmObject {
    var pd = scala.util.Random.nextInt(1024)
}

class Sequencer_a extends SvmComponent {
    val ap = new SvmAnalysisPort[txn]()
    def dut = SvmRunTest.dut.asInstanceOf[SvmRtlForTest]
    
    override def runPhase(phase: SvmPhase): Unit = {
        // phase.setAutoObjection(this)
        phase.raiseObjection()
        super.runPhase(phase)
        val seq = List.tabulate[txn](100)({
            i => {
                val req = txn().setName(f"TXN_$i")
                req.pd = i
                req
            }
        })

        seq.foreach({
            t => 
                svmHigh(f"sqr_a sending ${t.pd}")
                ap.write(t)
                dut.clockDomain.waitSampling()
        })
        dut.clockDomain.waitSampling(1000)
        phase.dropObjection()
    }
}

class Driver_a extends SvmComponent {
    val fifo = new SvmAnalysisFifo[txn]()
    def dut = SvmRunTest.dut.asInstanceOf[SvmRtlForTest]
    override def runPhase(phase: SvmPhase): Unit = {
        super.runPhase(phase)
        while (true) {
            val req = fifo.get()
            svmHigh(f"drv_a sending ${req.pd}")
            dut.io.slv_a.valid #= true 
            dut.io.slv_a.payload #= req.pd
            waitUntil(dut.io.slv_a.ready.toBoolean)
            dut.clockDomain.waitSampling()
        }
    }
}

class Monitor_b extends SvmComponent {
    val ap = new SvmAnalysisPort[txn]()
    def dut = SvmRunTest.dut.asInstanceOf[SvmRtlForTest]
    override def runPhase(phase: SvmPhase): Unit = {
        super.runPhase(phase)
        while (true) {
            val rsp = txn()
            val randomDelay = scala.util.Random.nextInt(10) // ready has 10 cycle random stall
            dut.io.mst_b.ready #= false
            for(cyc <- 0 until randomDelay) {
                dut.clockDomain.waitSampling()
            }
            dut.io.mst_b.ready #= true
            waitUntil(dut.io.mst_b.valid.toBoolean)
            rsp.pd = dut.io.mst_b.payload.toInt
            svmHigh(f"mon_b get ${rsp.pd}")
            ap.write(rsp)
            dut.clockDomain.waitSampling()
        }
    }
}

class Subscriber_b extends SvmComponent {
    val fifo = new SvmAnalysisFifo[txn]()
    val ap = new SvmAnalysisPort[txn]()
    override def runPhase(phase: SvmPhase): Unit = {
        super.runPhase(phase)
        while (true) {
            val rsp = fifo.get()
            svmHigh(f"sub_b get ${rsp.pd}")
            ap.write(rsp)
        }
    }
}

class Scoreboard() extends SvmComponent() {
    val act_fifo = new SvmAnalysisFifo[txn]()
    val exp_fifo = new SvmAnalysisFifo[txn]()
    override def runPhase(phase: SvmPhase): Unit = {
        super.runPhase(phase)
        while (true) {
            val act_txn = act_fifo.get()
            val exp_txn = exp_fifo.peek()
            exp_txn match {
                case None => svmError(f"Data no match")
                case Some(value) => 
                    if (value.pd != act_txn.pd) svmError(f"Data mismatched: actual txn=${act_txn.pd}, expected txn=${value.pd}")
                    else svmHigh(f"act=${act_txn.pd} matches exp=${value.pd}")
            }
        }
    }
}

class Agent_a() extends SvmComponent() {
    val drv_a = new Driver_a()
    val sqr_a = new Sequencer_a()

    override def connectPhase(phase: SvmPhase): Unit = {
        super.connectPhase(phase)
        sqr_a.ap.connect(drv_a.fifo.export)
    }
}

class Agent_b() extends SvmComponent() {
    val mon_b = new Monitor_b()
    val sub_b = new Subscriber_b()
    override def connectPhase(phase: SvmPhase): Unit = {
        super.connectPhase(phase)
        mon_b.ap.connect(sub_b.fifo.export)
    }
}

class Environment() extends SvmComponent() {
    val agent_a = new Agent_a()
    val agent_b = new Agent_b()
    val scb = new Scoreboard()
    override def connectPhase(phase: SvmPhase): Unit = {
        super.connectPhase(phase)
        agent_a.sqr_a.ap.connect(scb.exp_fifo.export)
        agent_b.sub_b.ap.connect(scb.act_fifo.export)
    }
}

class A_test extends SvmTest {
    val env = new Environment()
    override def buildPhase(phase: SvmPhase): Unit = {
        this.printTopology()
    }
}

object SvmTlmTest extends App {
    SimConfig.withIVerilog.withWave.compile(SvmRtlForTest()).doSim({ dut =>
        dut.clockDomain.forkStimulus(10 ns)
        svmLogLevel("medium")
        SvmRunTest(dut, new A_test())
    })
}