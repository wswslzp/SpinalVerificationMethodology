package svm 

//PhaseRunnerContainer包含所有的PhaseRunner，按照次序依次执行所有的PhaseRunner
//
object SvmPhaseManager {
    val phaseBuild = new SvmDownUpPhase("build", false)
    val phaseConnect = new SvmUpDownPhase("connect", false)
    val phaseRun = new SvmUpDownPhase("run", true)
    val phaseCheck = new SvmUpDownPhase("check", false)
    
    val phases = scala.collection.mutable.LinkedHashSet[SvmPhase](
        phaseBuild, phaseConnect, phaseRun, phaseCheck
    )

    def addPhase(phase: SvmPhase) : Unit = phases.addOne(phase)
    def runAllPhase(svc: SvmComponent): Unit = {
        phases.foreach(_.run(svc))
    }
}
