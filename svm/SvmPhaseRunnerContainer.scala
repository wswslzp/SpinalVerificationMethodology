package svm 

trait SvmPhaseRunnerContainer {
    val thisComponent: SvmComponent with SvmPhaseRunnerContainer
    def buildPhase(phase: SvmPhase): Unit = {
        thisComponent.children.foreach(c => c.buildPhase(phase))
        this.buildPhase(phase)
    }
    def runPhase(phase: SvmPhase): Unit
    def checkPhase(phase: SvmPhase): Unit
}