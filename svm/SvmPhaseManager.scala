package svm 

//PhaseRunnerContainer包含所有的PhaseRunner，按照次序依次执行所有的PhaseRunner
//
object SvmPhaseManager {
    val phaseBuild = new SvmDownUpPhase("build", false)
    val phaseRun = new SvmDownUpPhase("run", true)
    val phaseCheck = new SvmUpDownPhase("check", false)
    
    val phases = scala.collection.mutable.LinkedHashSet[SvmPhase](
        phaseBuild, phaseRun, phaseCheck
    )

    def addPhase(phase: SvmPhase) : Unit = phases.addOne(phase)
    def runAllPhase(svc: SvmComponent): Unit = phases.foreach(_.run(svc))
}
