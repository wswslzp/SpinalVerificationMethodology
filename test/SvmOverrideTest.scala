import svm._
import svm.base._
import spinal.core._
import spinal.core.sim._

class B_subscriber extends Subscriber_b {
    override def runPhase(phase: SvmPhase): Unit = {
        svmHigh("Subscribe overrided")
        super.runPhase(phase)
        while (true) {
            val rsp = fifo.get()
            svmHigh(f"B_subscriber get ${rsp.pd}")
            ap.write(rsp)
        }
    }
}

class B_test extends A_test {
    // SvmFactory.overrideTypeByType("Subscriber_b", new B_subscriber)
    override val env = new Environment() {
        println("override env")
        override val agent_b = new Agent_b {
            println("override agent_b")
            override val sub_b = new B_subscriber {
                println("override sub_b")
            }
        }
    }
}

class Human {
    val name: String = "zliao"
    val gender: String = "unknown"
}
class Man extends Human {
    override val gender: String = "male"
}

class NV {
    val zliao = new Human
}

object SvmOverrideTest extends App {
    val nv = new NV {
        override val zliao: Human = new Man {
            override val name: String = "zhengpeng liao"
        }
    }
    println(nv.zliao.gender, nv.zliao.name)
    // val nv1 = new NV {
    //     override val zliao = new Man
    // }
    SimConfig.withIVerilog.withWave.compile(SvmRtlForTest()).doSim({ dut =>

        dut.clockDomain.forkStimulus(10 ns)
        svmLogLevel("medium")
        SvmRunTest(dut, new B_test())
    })
}