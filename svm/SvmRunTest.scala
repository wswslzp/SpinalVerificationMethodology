package svm 

object SvmRunTest {
    def apply(): Unit = {
        SvmPhaseManager.runAllPhase(SvmComponent.getTopSvc)
    }
}