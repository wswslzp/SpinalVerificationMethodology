import svm._
import svm.base._
import svm.tlm._
import svm.comps._
import spinal.core._
import spinal.core.sim._
import spinal.lib._
import svm.seq.SvmSequencer

case class SvmRtlForTest() extends Component {
    val io = new Bundle {
        val a = in Bits(8 bit)
        val b = out Bits(8 bit)
        
        val slv_a = slave Stream(Bits(32 bit))
        val mst_b = master Stream(Bits(32 bit))
    }
    io.b := RegNext(io.a)
    
    io.mst_b <-/< io.slv_a
}

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
                logger.info(f"sqr_a sending ${t.pd}")
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
            logger.info(f"drv_a sending ${req.pd}")
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
            logger.info(f"mon_b get ${rsp.pd}")
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
            logger.info(f"sub_b get ${rsp.pd}")
            ap.write(rsp)
        }
    }
}

class Agent_a() extends SvmAgent {
    val drv_a = ! new Driver_a()
    val sqr_a = ! new Sequencer_a()

    override def buildPhase(phase: SvmPhase): Unit = {
        logger.trace(f"[Connection] ${sqr_a.ap.getFullName()} >> ${drv_a.fifo.export.getFullName()}")
    }

    override def connectPhase(phase: SvmPhase): Unit = {
        super.connectPhase(phase)
        sqr_a.ap >> drv_a.fifo.export
    }
}

class Agent_b() extends SvmAgent {
    val mon_b = ! new Monitor_b()
    val sub_b = ! new Subscriber_b()

    override def buildPhase(phase: SvmPhase): Unit = {
        logger.trace(f"[Connection] ${mon_b.ap.getFullName()} >> ${sub_b.fifo.export.getFullName()}")
    }

    override def connectPhase(phase: SvmPhase): Unit = {
        super.connectPhase(phase)
        logger.debug(f"mon_b.ap name is ${mon_b.ap.getFullName()}")
        logger.debug(f"sub_b.fifo.export name is ${sub_b.fifo.export.getFullName()}")
        mon_b.ap >> sub_b.fifo.export
    }
}

class Environment() extends SvmEnv {
    val agent_a = ! new Agent_a()
    val agent_b = ! new Agent_b()
    val scb = ! new SvmScoreboard[txn]() {
        override def buildPhase(phase: SvmPhase): Unit = {
            logger.debug(f"exp_fifo name is ${expFifo.getFullName()}")
            logger.debug(f"act_fifo name is ${actFifo.getFullName()}")
        }
        onMatched((a,b) => logger.debug("matched"))
        onMismatched((a,b) => logger.error("mismatch"))
    }
    override def connectPhase(phase: SvmPhase): Unit = {
        super.connectPhase(phase)
        logger.debug(f"[Connection] scb.expFifo.export name is ${scb.expFifo.export.getFullName()}")
        logger.debug(f"[Connection] sub_b.fifo.export name is ${agent_b.sub_b.fifo.export.getFullName()}")
        agent_a.sqr_a.ap >> scb.expFifo.export
        agent_b.sub_b.ap >> scb.actFifo.export
    }
}

class A_test extends SvmTest {
    val env = ! new Environment()
    override def buildPhase(phase: SvmPhase): Unit = {
        super.buildPhase(phase)
        this.printTopology()
    }
}
