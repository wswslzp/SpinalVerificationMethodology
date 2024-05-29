import svm._
import svm.base._
import spinal.core._
import spinal.core.sim._
import svm.tlm.SvmAnalysisFifo

class B_subscriber extends Subscriber_b {
    override def runPhase(phase: SvmPhase): Unit = {
        super.runPhase(phase)
        logger.info("Subscribe overrided")
        while (true) {
            val rsp = fifo.get()
            logger.info(f"B_subscriber get ${rsp.pd}")
            ap.write(rsp)
        }
    }
}

class B_test extends A_test {
    override def buildPhase(phase: SvmPhase): Unit = {
        super.buildPhase(phase)
        factory.overrideInstByName("SvmRoot.B_test.env.agent_b.sub_b", new B_subscriber)
        // factory.overrideInstByGlobPattern("SvmRoot.*.sub_b.*", new B_subscriber)
    }
}

class Human extends ValCallback{
    var name: String = "unknown"
    val gender: String = "unknown"
    
    override def valCallback[T](ref: T, name: String): T = {
        ref
    }
    def say(): Unit = {println("NAAA")}
    
    println(f"In Human() init, (name: $name), (gender: $gender)")
}
class Man extends Human {
    override val gender: String = "male"
    override def say(): Unit = {println("Bitch")}
    println(f"In Man() init, (name: $name), (gender: $gender)")
}

class Woman extends Human {
    override val gender: String = "female"
    override def say(): Unit = {println("Nigga")}
    println(f"In Woman() init, (name: $name), (gender: $gender)")
}

class School extends ValCallback with PostInitCallback{
    // First phase stu1 not null
    val stu1, stu3, stu4 = new Man
    val stu2, stu5 = new Woman
    
    // Third phase, stu1 is null
    // stu1.name = "stu1"
    stu2.name = "stu2"
    stu3.name = "stu3"
    stu4.name = "stu4"
    stu5.name = "stu5"
    
    // Second phase, stu1 not null
    override def valCallback[T](ref: T, name: String): T = {
        val rref = ref.asInstanceOf[Human]
        println(f"student name ${rref.name}, gender ${rref.gender}")
        ref
    }
    
    override def postInitCallback(): this.type = {
        println("Into School post init callback")
        this
    }
    
    //Third phase, stu1 is null
    if (stu1 == null) {
        println("stu1 now is null")
    } else {
        println(f"stu1 now is not null, has name ${stu1.name}")
    }
    println("Ending School initialization")
}

class AA extends SvmObject
class BB extends AA

class C_test extends SvmComponent {
    val aa = ! new AA 
    val bb = ! new BB 
    println(f"aa type is ${aa.getTypeName()}")
    println(f"bb type is ${bb.getTypeName()}")
}

class CC_test extends C_test {
    // SvmFactory.overrideInstByGlobPattern("aa", new BB)
    println(f"aa type is ${aa.getTypeName()}")
    println(f"bb type is ${bb.getTypeName()}")
}

// First initialize one var's Father/Super class, 
// Second initialze this var's Children class,
// Third call ValCallBack on this var, right after it initialized
// If some of the var is overrided in this class's children class, then this var would be set as null
// Then go on next var initialization and go on this class (School) inititalization until end
object SvmOverrideTest extends App {
    // val nv = new School
    // nv.stu1.say()
    // println(nv.stu1.name)
    // val cc_test = new CC_test
    // val nv1 = new School {
    //     // Override one var:
    //     // first, set the original var = null
    //     // second, end Father class (School) initialization.
    //     println("In school override")
    //     if (stu1 == null) {
    //         println("stu1 now is null")
    //     } else {
    //         println(f"stu1 now is not null, has name ${stu1.name}")
    //     }
    //
    //     override val stu1: Man = new Man {
    //         println("In stu1 override")
    //         override def say() : Unit = {println("Damn")}
    //     }
    //     if (stu1 == null) {
    //         println("stu1 now is null")
    //     } else {
    //         println(f"stu1 now is not null, has name ${stu1.name}")
    //     }
    //     println("Ending new School initialization")
    // }
    // nv1.stu1.say()
    // println(nv1.stu1.name)
    // // println(nv.zliao.gender, nv.zliao.name)
    // // val nv1 = new NV {
    // //     override val zliao = new Man
    // // }
    SimConfig.withIVerilog.withWave.compile(SvmRtlForTest()).doSim({ dut =>

        dut.clockDomain.forkStimulus(10 ns)
        setSvmLogLevel("low")
        setSimLogFile("logs/SvmOverrideTest.log")
        val test = new B_test
        test.printTopology()
        SvmRunTest(dut, test)
    })
}