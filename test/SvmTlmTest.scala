import svm._
import svm.tlm._
import spinal.core._
import spinal.core.sim._
import svm.SvmComponent.svm_root

case class txn(name: String) extends SvmObject(name) {
    var pd = scala.util.Random.nextInt(1024)
}

class Sequencer_a(parent: SvmComponent) extends SvmComponent("seqr_a", parent) {
    val ap = new SvmAnalysisPort[txn]("seqr_a.ap")
    def dut = SvmRunTest.dut.asInstanceOf[SvmRtlForTest]
    
    override def runPhase(phase: SvmPhase): Unit = {
        // phase.setAutoObjection(this)
        phase.raiseObjection()
        super.runPhase(phase)
        val seq = List.tabulate[txn](100)({
            i => {
                val req = txn(f"TXN_$i")
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

class Driver_a(parent: SvmComponent) extends SvmComponent("drv_a", parent) {
    val fifo = new SvmAnalysisFifo[txn]("drv_a.fifo")
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

class Monitor_b(parent: SvmComponent) extends SvmComponent("mon_b", parent) {
    val ap = new SvmAnalysisPort[txn]("mon_b.ap")
    def dut = SvmRunTest.dut.asInstanceOf[SvmRtlForTest]
    override def runPhase(phase: SvmPhase): Unit = {
        super.runPhase(phase)
        while (true) {
            val rsp = txn("rsp")
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

class Subscriber_b(parent: SvmComponent) extends SvmComponent("sub_b", parent) {
    val fifo = new SvmAnalysisFifo[txn]("sub_b.fifo")
    val ap = new SvmAnalysisPort[txn]("sub_b.ap")
    override def runPhase(phase: SvmPhase): Unit = {
        super.runPhase(phase)
        while (true) {
            val rsp = fifo.get()
            svmHigh(f"sub_b get ${rsp.pd}")
            ap.write(rsp)
        }
    }
}

class Scoreboard(parent: SvmComponent) extends SvmComponent("scb", parent) {
    val act_fifo = new SvmAnalysisFifo[txn]("scb.act_fifo")
    val exp_fifo = new SvmAnalysisFifo[txn]("scb.exp_fifo")
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

class Agent_a(parent: SvmComponent) extends SvmComponent("agent_a", parent) {
    val drv_a = new Driver_a(this)
    val sqr_a = new Sequencer_a(this)

    override def connectPhase(phase: SvmPhase): Unit = {
        super.connectPhase(phase)
        sqr_a.ap.connect(drv_a.fifo.export)
    }
}

class Agent_b(parent: SvmComponent) extends SvmComponent("agent_b", parent) {
    val mon_b = new Monitor_b(this)
    val sub_b = new Subscriber_b(this)
    override def buildPhase(phase: SvmPhase): Unit = {
        svmError(f"size of children is ${children.size}")
    }
    override def connectPhase(phase: SvmPhase): Unit = {
        super.connectPhase(phase)
        mon_b.ap.connect(sub_b.fifo.export)
    }
}

class Environment(parent: SvmComponent) extends SvmComponent("env", parent) {
    val agent_a = new Agent_a(this)
    val agent_b = new Agent_b(this)
    val scb = new Scoreboard(this)
    override def buildPhase(phase: SvmPhase): Unit = {
        svmError(f"size of children is ${children.size}")
    }
    override def connectPhase(phase: SvmPhase): Unit = {
        super.connectPhase(phase)
        agent_a.sqr_a.ap.connect(scb.exp_fifo.export)
        agent_b.sub_b.ap.connect(scb.act_fifo.export)
    }
}

class A_test() extends SvmComponent("A_test", svm_root) {
    val env = new Environment(this)
    override def buildPhase(phase: SvmPhase): Unit = {
        this.printTopology()
    }
    def runTest(dut: SvmRtlForTest) : Unit = {
        SvmRunTest.dut = dut
        SvmRunTest()
    }
}

object SvmTlmTest extends App {
    SimConfig.withIVerilog.withWave.compile(SvmRtlForTest()).doSim({ dut =>
        dut.clockDomain.forkStimulus(10 ns)
        svmLogLevel("medium")
        new A_test().runTest(dut)
        // runrun(dut)
    })
}