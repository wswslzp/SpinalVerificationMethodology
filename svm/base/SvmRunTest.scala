package svm.base
import spinal.core._

object SvmRunTest {
    var dut = null.asInstanceOf[Component]
    def apply(): Unit = {
        SvmComponent.getTopSvc.foreach({
            top => 
                SvmPhaseManager.runAllPhase(top)
        })
    }
    def apply[D <: Component, T <: SvmComponent](top: D, tb: T): Unit = {
        dut = top
        tb.register()
        apply()
    }
}