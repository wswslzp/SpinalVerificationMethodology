package svm.base
import spinal.core._
import svm.SvmComponentWrapper

object SvmRunTest {
    var dut = null.asInstanceOf[Component]
    def apply(): Unit = {
        SvmComponent.getTopSvc.foreach({
            top => 
                SvmPhaseManager.runAllPhase(top)
        })
    }
    def apply[D <: Component, S <: SvmComponent](top: D, tb: S): Unit = {
        dut = top
        tb.setName(tb.getTypeName()).registerPhases()
        apply()
    }
    def apply[D <: Component, S <: SvmComponent, T <: SvmObjectWrapper[S]](top: D, tb: T): Unit = {
        dut = top
        tb.setName(tb.getTypeName()).registerPhases()
        apply()
    }
}