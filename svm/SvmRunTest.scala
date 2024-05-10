package svm 
import spinal.core._

object SvmRunTest {
    var dut = null.asInstanceOf[Component]
    def apply(): Unit = {
        SvmPhaseManager.runAllPhase(SvmComponent.getTopSvc)
    }
    def apply[T <: Component](top: T): Unit = {
        dut = top
    }
}