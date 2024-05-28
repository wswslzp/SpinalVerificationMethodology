package svm.base
import spinal.core._
import svm._

object SvmRunTest {
    var dut = null.asInstanceOf[Component]
    def runAllTest(): Unit = {
        SvmComponent.getTopSvc.foreach({
            top => 
                SvmPhaseManager.runAllPhase(top)
        })
    }
    def apply[D <: Component, S <: SvmComponent](top: D, tb: S): Unit = {
        tb.setName(tb.getTypeName()).registerPhases()
        SvmRoot.addOneTest(tb)
        dut = top
        runAllTest()
    }
}