import svm.base._
import spinal.core._
import spinal.core.sim._
import svm.setSvmLogLevel

class ARTL extends Component {
    val io = new Bundle {
        val a = in Bits(8 bit)
        val b = out Bits(8 bit)
    }
    io.b := RegNext(io.a)
}

class TbEnv extends SvmComponent {
    val drv_a = !new SvmComponent {
        def rtl: ARTL = SvmRunTest.dut.asInstanceOf[ARTL]
        override def runPhase(phase: SvmPhase): Unit = {
            // drive the rtl
            val say = "hello, world"
            val bytes = say.getBytes().map(_.toInt)
            super.runPhase(phase)
            phase.raiseObjection()
            bytes foreach {byte =>
                rtl.io.a #= byte
                println(f"writing $byte")
                rtl.clockDomain.waitSampling()
            }
            rtl.clockDomain.waitSampling(10)
            phase.dropObjection()
        }
    }
    
    val mon_b = !new SvmComponent {
        def rtl: ARTL = SvmRunTest.dut.asInstanceOf[ARTL]
        override def runPhase(phase: SvmPhase): Unit = {
            // monitor the rtl
            val bytes = scala.collection.mutable.Queue[Int]()
            super.runPhase(phase)
            while(true) {
                rtl.clockDomain.waitSampling()
                val byte = rtl.io.b.toInt
                println(f"Got byte ${byte.toChar}")
            }
        }
    }
}
    
class ATest(rtl: ARTL) {
    val tb = !new TbEnv
    setSvmLogLevel("low")
    tb.getActualObj.printTopology()
    SvmRunTest(rtl, tb.getActualObj)
}

object SvmRunPhaseTest extends App{
    SimConfig.withIVerilog.compile(new ARTL).doSim {a =>
        a.clockDomain.forkStimulus(10)
        setSvmLogLevel("low")
        new ATest(a)
    }
}