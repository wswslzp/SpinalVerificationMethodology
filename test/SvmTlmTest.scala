import svm._
import svm.base._
import svm.tlm._
import svm.comps._
import spinal.core._
import spinal.core.sim._
import svm.seq.SvmSequencer

case class txn() extends SvmObject {
    var pd = scala.util.Random.nextInt(1024)
}

class Sequencer_a extends SvmSequencer[txn] {
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

class Driver_a extends SvmDriver[txn] {
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

class Monitor_b extends SvmMonitor[txn] {
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

class Subscriber_b extends SvmSubscriber[txn] {
    override def runPhase(phase: SvmPhase): Unit = {
        super.runPhase(phase)
        while (true) {
            val rsp = fifo.get()
            svmHigh(f"sub_b get ${rsp.pd}")
            ap.write(rsp)
        }
    }
}

class Agent_a() extends SvmAgent {
    val drv_a = new Driver_a()
    val sqr_a = new Sequencer_a()

    override def connectPhase(phase: SvmPhase): Unit = {
        super.connectPhase(phase)
        sqr_a.ap >> drv_a.fifo.export
    }
}

class Agent_b() extends SvmAgent {
    val mon_b = new Monitor_b()
    val sub_b = new Subscriber_b()
    override def connectPhase(phase: SvmPhase): Unit = {
        super.connectPhase(phase)
        mon_b.ap >> sub_b.fifo.export
    }
}

class Environment() extends SvmEnv {
    val agent_a = new Agent_a()
    val agent_b = new Agent_b()
    val scb = new SvmScoreboard[txn]()
    override def connectPhase(phase: SvmPhase): Unit = {
        super.connectPhase(phase)
        agent_a.sqr_a.ap >> scb.expFifo.export
        agent_b.sub_b.ap >> scb.actFifo.export
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