import svm.base.SvmComponent
import svm.base.SvmPhase
import svm.base.SvmRunTest
import svm.base.SvmPhaseManager

class subA extends SvmComponent {
    override def buildPhase(phase: SvmPhase): Unit = {
        super.buildPhase(phase)
        println(s"build sub svc ${getFullName()}")
    }
    override def checkPhase(phase: SvmPhase): Unit = {
        super.checkPhase(phase)
        println(s"check sub svc ${getFullName()}")
    }
    override def runPhase(phase: SvmPhase): Unit = {
        super.runPhase(phase)
        println(s"run sub svc ${getFullName()}")
    }
}

class A extends SvmComponent {
    val A_a = new subA
    val A_b = new subA
    val A_c = new subA
}
// test:compile
object SvmComponentTest extends App {
    val a = new A()
    a.printTopology()
    // SvmPhaseManager.phaseRun.skipping = true
    val svc = SvmPhaseManager.phaseBuild.phaseTasks.map(_._1)
    val tasks = SvmPhaseManager.phaseBuild.phaseTasks.map(_._2)
    // svc.foreach(println(_))
    println(f"svc num is ${svc.size}")
    SvmRunTest()
}